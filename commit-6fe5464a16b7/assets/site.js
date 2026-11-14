// Behaviour of the documentation pages: theme, navigation, tabs, copy buttons, search and the version picker.
(function () {
  'use strict';

  const body = document.body;
  const root = body.dataset.root || './';
  const currentPath = body.dataset.path || '';
  const currentVersion = body.dataset.version || '';

  const storage = {
    get(key) {
      try {
        return localStorage.getItem(key);
      } catch (e) {
        return null;
      }
    },
    set(key, value) {
      try {
        localStorage.setItem(key, value);
      } catch (e) {
        // storage can be unavailable, the setting just isn't remembered then
      }
    }
  };

  // ---------------------------------------------------------------- theme

  const themeSelect = document.querySelector('[data-theme-select]');
  const systemTheme = () => (matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark');

  function applyTheme(choice) {
    document.documentElement.dataset.theme = choice === 'light' || choice === 'dark' ? choice : systemTheme();
  }

  if (themeSelect) {
    const stored = storage.get('starlight-theme');
    themeSelect.value = stored === 'light' || stored === 'dark' ? stored : 'auto';
    themeSelect.addEventListener('change', () => {
      const choice = themeSelect.value;
      storage.set('starlight-theme', choice === 'auto' ? '' : choice);
      applyTheme(choice);
    });
    matchMedia('(prefers-color-scheme: light)').addEventListener('change', () => {
      if (themeSelect.value === 'auto') {
        applyTheme('auto');
      }
    });
  }

  // ---------------------------------------------------------------- mobile menu

  const menuButton = document.querySelector('[data-open-menu]');
  if (menuButton) {
    menuButton.addEventListener('click', () => {
      const open = !body.hasAttribute('data-menu-open');
      body.toggleAttribute('data-menu-open', open);
      menuButton.setAttribute('aria-expanded', String(open));
    });
    addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && body.hasAttribute('data-menu-open')) {
        body.removeAttribute('data-menu-open');
        menuButton.setAttribute('aria-expanded', 'false');
        menuButton.focus();
      }
    });
  }

  const currentLink = document.querySelector('.sidebar a[aria-current="page"]');
  if (currentLink) {
    const sidebar = document.querySelector('.sidebar');
    const linkTop = currentLink.getBoundingClientRect().top - sidebar.getBoundingClientRect().top;
    if (linkTop > sidebar.clientHeight - 80) {
      sidebar.scrollTop = linkTop - sidebar.clientHeight / 3;
    }
  }

  // ---------------------------------------------------------------- tabs

  function selectTab(tabs, label) {
    const buttons = tabs.querySelectorAll(':scope > .tab-list > .tab');
    const panels = tabs.querySelectorAll(':scope > .tab-panel');
    let index = -1;
    buttons.forEach((button, i) => {
      if (button.dataset.label === label) {
        index = i;
      }
    });
    if (index < 0) {
      return;
    }
    buttons.forEach((button, i) => button.setAttribute('aria-selected', String(i === index)));
    panels.forEach((panel, i) => (panel.hidden = i !== index));
  }

  const allTabs = document.querySelectorAll('.tabs');
  const preferredTab = storage.get('minified-tab');
  allTabs.forEach((tabs) => {
    if (preferredTab) {
      selectTab(tabs, preferredTab);
    }
    tabs.querySelector(':scope > .tab-list').addEventListener('click', (e) => {
      const button = e.target.closest('.tab');
      if (!button) {
        return;
      }
      const label = button.dataset.label;
      const before = button.getBoundingClientRect().top;
      // keep code samples in one language across the page, e.g. Java or Kotlin
      allTabs.forEach((other) => selectTab(other, label));
      scrollBy(0, button.getBoundingClientRect().top - before);
      storage.set('minified-tab', label);
    });
  });

  // ---------------------------------------------------------------- copy buttons

  document.querySelectorAll('.copy-button').forEach((button) => {
    button.addEventListener('click', async () => {
      const code = button.parentElement.querySelector('pre code');
      try {
        await navigator.clipboard.writeText(code.textContent);
        button.classList.add('copied');
        setTimeout(() => button.classList.remove('copied'), 1500);
      } catch (e) {
        // clipboard access denied, nothing to show
      }
    });
  });

  // ---------------------------------------------------------------- table of contents

  const tocLinks = Array.from(document.querySelectorAll('.toc a'));
  if (tocLinks.length && 'IntersectionObserver' in window) {
    const targets = tocLinks
      .map((link) => document.getElementById(decodeURIComponent(link.getAttribute('href').slice(1))))
      .filter(Boolean);
    const visible = new Set();
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => (entry.isIntersecting ? visible.add(entry.target) : visible.delete(entry.target)));
        let active = null;
        for (const target of targets) {
          if (visible.has(target)) {
            active = target;
            break;
          }
        }
        if (!active) {
          return;
        }
        tocLinks.forEach((link) => {
          const match = decodeURIComponent(link.getAttribute('href').slice(1)) === active.id;
          link.setAttribute('aria-current', String(match));
          if (match) {
            const panel = link.closest('.right-sidebar');
            const top = link.offsetTop - panel.scrollTop;
            if (top < 0 || top > panel.clientHeight - 40) {
              panel.scrollTop = link.offsetTop - panel.clientHeight / 2;
            }
          }
        });
      },
      { rootMargin: '-64px 0px -66% 0px' }
    );
    targets.forEach((target) => observer.observe(target));
  }

  // ---------------------------------------------------------------- search

  const dialog = document.querySelector('.search-dialog');
  const input = dialog && dialog.querySelector('.search-input');
  const results = dialog && dialog.querySelector('.search-results');
  let selected = 0;
  let shown = [];

  function openSearch() {
    if (!dialog || dialog.open) {
      return;
    }
    dialog.showModal();
    input.select();
    render();
  }

  function closeSearch() {
    if (dialog && dialog.open) {
      dialog.close();
    }
  }

  function escapeHtml(text) {
    return text.replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c]);
  }

  function highlight(text, query) {
    const index = text.toLowerCase().indexOf(query);
    if (!query || index < 0) {
      return escapeHtml(text);
    }
    return escapeHtml(text.slice(0, index)) + '<mark>' + escapeHtml(text.slice(index, index + query.length)) + '</mark>'
      + escapeHtml(text.slice(index + query.length));
  }

  const kindWeight = { class: 6, interface: 6, record: 6, enum: 6, 'abstract class': 6, exception: 5, annotation: 5,
    event: 7, guide: 8, page: 8, module: 5, package: 3, section: 4, method: 2, field: 1, constant: 1, component: 2 };

  function score(entry, query, words) {
    const title = entry[0].toLowerCase();
    const detail = entry[3].toLowerCase();
    const bare = title.replace(/\(\)$/, '');
    const member = bare.includes('.') && entry[1] !== 'package' ? bare.slice(bare.lastIndexOf('.') + 1) : bare;
    // camel case initials, e.g. "lc" finds LaunchConfiguration
    const initials = entry[0].replace(/\(\)$/, '').split('.').pop().replace(/[^A-Z]/g, '').toLowerCase();
    let s = 0;
    if (bare === query || member === query) {
      s = 100;
    } else if (bare.startsWith(query) || member.startsWith(query)) {
      s = 70;
    } else if (query.length > 1 && initials.startsWith(query)) {
      s = 60;
    } else if (title.includes(query)) {
      s = 45;
    } else if (words.every((w) => title.includes(w) || detail.includes(w))) {
      s = 25;
    } else {
      return 0;
    }
    return s + (kindWeight[entry[1]] || 0) - Math.min(title.length, 60) / 30;
  }

  function render() {
    const index = window.MINIFIED_SEARCH || [];
    const query = input.value.trim().toLowerCase();
    if (!query) {
      shown = index.filter((e) => e[1] === 'guide' || e[1] === 'page').slice(0, 12);
    } else {
      const words = query.split(/\s+/);
      shown = index
        .map((entry) => [score(entry, query, words), entry])
        .filter((pair) => pair[0] > 0)
        .sort((a, b) => b[0] - a[0])
        .slice(0, 60)
        .map((pair) => pair[1]);
    }
    selected = 0;
    if (!shown.length) {
      results.innerHTML = '<li class="search-empty">No results for “' + escapeHtml(input.value) + '”</li>';
      return;
    }
    results.innerHTML = shown
      .map((entry, i) => '<li><a class="search-result" role="option" href="' + root + entry[2] + '" aria-selected="' + (i === 0)
        + '"><strong>' + highlight(entry[0], query) + '</strong><span class="api-badge api-badge-kind">' + escapeHtml(entry[1])
        + '</span>' + (entry[3] ? '<small>' + escapeHtml(entry[3]) + '</small>' : '') + '</a></li>')
      .join('');
  }

  function move(delta) {
    const items = results.querySelectorAll('.search-result');
    if (!items.length) {
      return;
    }
    items[selected].setAttribute('aria-selected', 'false');
    selected = (selected + delta + items.length) % items.length;
    items[selected].setAttribute('aria-selected', 'true');
    items[selected].scrollIntoView({ block: 'nearest' });
  }

  if (dialog) {
    document.querySelectorAll('[data-open-search]').forEach((b) => b.addEventListener('click', openSearch));
    dialog.querySelector('[data-close-search]').addEventListener('click', closeSearch);
    dialog.addEventListener('click', (e) => {
      if (e.target === dialog) {
        closeSearch();
      }
    });
    input.addEventListener('input', render);
    input.addEventListener('keydown', (e) => {
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        move(1);
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        move(-1);
      } else if (e.key === 'Enter') {
        const item = results.querySelectorAll('.search-result')[selected];
        if (item) {
          e.preventDefault();
          location.href = item.href;
          closeSearch();
        }
      }
    });
    results.addEventListener('click', (e) => {
      if (e.target.closest('.search-result')) {
        closeSearch();
      }
    });
    addEventListener('keydown', (e) => {
      const typing = /^(INPUT|TEXTAREA|SELECT)$/.test(document.activeElement.tagName) || document.activeElement.isContentEditable;
      if ((e.key === 'k' && (e.metaKey || e.ctrlKey)) || (e.key === '/' && !typing)) {
        e.preventDefault();
        dialog.open ? closeSearch() : openSearch();
      }
    });
  }

  // ---------------------------------------------------------------- versions

  // Every version lives in its own directory next to versions.json, e.g. /3.1.0/ and /3.0.0/
  const versionSelect = document.querySelector('[data-version-select]');
  if (versionSelect && location.protocol !== 'file:') {
    const versionsUrl = root + '../versions.json';
    fetch(versionsUrl, { cache: 'no-cache' })
      .then((response) => (response.ok ? response.json() : null))
      .then((data) => {
        if (!data || !Array.isArray(data.versions) || !data.versions.length) {
          return;
        }
        versionSelect.innerHTML = data.versions
          .map((v) => '<option value="' + escapeHtml(v) + '"' + (v === currentVersion ? ' selected' : '') + '>v' + escapeHtml(v)
            + (v === data.latest ? ' (latest)' : '') + '</option>')
          .join('');
        if (!data.versions.includes(currentVersion)) {
          versionSelect.insertAdjacentHTML('afterbegin', '<option selected>v' + escapeHtml(currentVersion) + '</option>');
        }
        if (data.latest && data.latest !== currentVersion) {
          const banner = document.querySelector('.version-banner');
          banner.innerHTML = 'You are reading the documentation of Minified v' + escapeHtml(currentVersion)
            + '. The latest version is <a href="' + root + '../' + encodeURIComponent(data.latest) + '/' + currentPath + '" data-version-link="'
            + escapeHtml(data.latest) + '">v' + escapeHtml(data.latest) + '</a>.';
          banner.hidden = false;
          banner.querySelector('a').addEventListener('click', (e) => {
            e.preventDefault();
            switchVersion(data.latest);
          });
        }
      })
      .catch(() => {
        // a local build has no versions.json, the picker just shows the built version
      });

    versionSelect.addEventListener('change', () => switchVersion(versionSelect.value));
  }

  // Opens the same page in another version, or that version's home page when the page doesn't exist there
  function switchVersion(version) {
    const base = root + '../' + encodeURIComponent(version) + '/';
    const target = base + currentPath + location.hash;
    fetch(base + currentPath, { method: 'HEAD' })
      .then((response) => (location.href = response.ok ? target : base))
      .catch(() => (location.href = base));
  }
})();
