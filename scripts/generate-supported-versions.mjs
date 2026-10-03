#!/usr/bin/env node
import { readFileSync, readdirSync, writeFileSync, existsSync } from 'node:fs';
import { join } from 'node:path';

const inputDir   = process.argv[2] ?? 'matrix-results';
const outputFile = process.argv[3] ?? 'SUPPORTED_VERSIONS.svg';

if (!existsSync(inputDir)) {
    console.error(`input dir not found: ${inputDir}`);
    process.exit(1);
}

const results = [];
for (const file of readdirSync(inputDir)) {
    if (!file.endsWith('.json')) continue;
    try {
        results.push(JSON.parse(readFileSync(join(inputDir, file), 'utf8')));
    } catch (e) {
        console.warn(`Skipping ${file}: ${e.message}`);
    }
}

const cmp = (a, b) => a.localeCompare(b, undefined, { numeric: true });
// pre-release versions are before 1.0: rubydung, classic, indev/infdev, alpha, beta
const ERAS = [/^rd-/, /^c\d/, /^in(f)?-/, /^a\d/, /^b\d/];
const eraOf = v => { const i = ERAS.findIndex(e => e.test(v)); return i < 0 ? ERAS.length : i; };
const cmpVersion = (a, b) => eraOf(a) - eraOf(b) || cmp(a, b);
// rd-132211, inf-20100618, c0.30_01c don't fit in a cell
const label = v => v.length > 7 ? v.split(/[-_]/)[0] : v;
const loaders  = [...new Set(results.map(r => r.loader))].sort(cmp);
const versions = [...new Set(results.map(r => r.mcVersion))].sort(cmpVersion);

const lookup = new Map();
for (const r of results) lookup.set(`${r.loader}\u0000${r.mcVersion}`, r);

const esc = s => String(s).replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&apos;' }[c]));

const BG        = '#0a0e17';
const CARD      = '#111827';
const CARD_BORD = '#1f2937';
const TEXT_HI   = '#f1f5f9';
const TEXT_MID  = '#94a3b8';
const TEXT_DIM  = '#64748b';
const GRID_DOT  = '#475569';

const COLORS = {
    pass:    { bg: '#22c55e', fg: '#052e16', glyph: '\u2713' },
    fail:    { bg: '#ef4444', fg: '#450a0a', glyph: '\u2715' },
    skipped: { bg: '#f59e0b', fg: '#451a03', glyph: '\u223c' },
    missing: { bg: '#1e293b', fg: GRID_DOT, glyph: '\u00b7' },
};

const statusOf = (l, v) => lookup.get(`${l}\u0000${v}`)?.status ?? 'missing';
const colorOf  = s => COLORS[s] ?? COLORS.missing;

const counts = { pass: 0, fail: 0, skipped: 0, missing: 0 };
for (const l of loaders) for (const v of versions) counts[statusOf(l, v)]++;

const PAD        = 40;
const TITLE_H    = 44;
const SUBTITLE_H = 30;
const LEGEND_H   = 52;
const HEADER_H   = 34;
const CELL_W     = 56;
const CELL_H     = 40;
const GAP_X      = 8;
const GAP_Y      = 8;
const LOADER_COL = 120;
const GUTTER     = 24;
const CARD_PAD   = 32;

const gridW = versions.length * CELL_W + Math.max(0, versions.length - 1) * GAP_X;
const gridH = loaders.length  * CELL_H + Math.max(0, loaders.length  - 1) * GAP_Y;

const innerW = LOADER_COL + GUTTER + gridW;
const innerH = HEADER_H + gridH;

const cardW = innerW + CARD_PAD * 2;
const cardH = innerH + CARD_PAD * 2;

const cardX = PAD;
const cardY = PAD + TITLE_H + SUBTITLE_H + LEGEND_H;

const totalW = cardW + PAD * 2;
const totalH = cardY + cardH + PAD;

const parts = [];
parts.push(`<svg xmlns="http://www.w3.org/2000/svg" width="${totalW}" height="${totalH}" viewBox="0 0 ${totalW} ${totalH}" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Inter, Helvetica, Arial, sans-serif">`);

parts.push(`<defs>`);
parts.push(`<filter id="cardShadow" x="-6%" y="-6%" width="112%" height="118%">`);
parts.push(`<feDropShadow dx="0" dy="6" stdDeviation="14" flood-color="#000000" flood-opacity="0.45"/>`);
parts.push(`</filter>`);
parts.push(`<linearGradient id="bgGrad" x1="0" y1="0" x2="0" y2="1">`);
parts.push(`<stop offset="0%" stop-color="#0d1420"/>`);
parts.push(`<stop offset="100%" stop-color="#06080d"/>`);
parts.push(`</linearGradient>`);
parts.push(`</defs>`);

parts.push(`<rect width="${totalW}" height="${totalH}" fill="url(#bgGrad)"/>`);

let y = PAD;

parts.push(`<text x="${PAD}" y="${y + 30}" font-size="32" font-weight="800" fill="${TEXT_HI}" letter-spacing="-0.8">Supported Versions</text>`);
y += TITLE_H;

parts.push(`<text x="${PAD}" y="${y + 2}" font-size="14" font-weight="500" fill="${TEXT_MID}">${loaders.length} loaders \u00b7 ${versions.length} Minecraft versions</text>`);
y += SUBTITLE_H;

const legendItems = [
    ['Passed',     counts.pass,    COLORS.pass],
    ['Failed',     counts.fail,    COLORS.fail],
    ['Skipped',    counts.skipped, COLORS.skipped],
    ['Not tested', counts.missing, COLORS.missing],
];
let lx = PAD;
const ly = y + 24;
const chipR = 9;
for (const [label, count, col] of legendItems) {
    parts.push(`<rect x="${lx}" y="${ly - chipR - 3}" width="${chipR * 2}" height="${chipR * 2}" rx="6" fill="${col.bg}"/>`);
    parts.push(`<text x="${lx + chipR}" y="${ly + 3}" font-size="11" font-weight="700" fill="${col.fg}" text-anchor="middle">${col.glyph}</text>`);
    parts.push(`<text x="${lx + chipR * 2 + 10}" y="${ly + 4}" font-size="13" fill="${TEXT_HI}"><tspan font-weight="700">${count}</tspan><tspan fill="${TEXT_MID}" font-weight="500"> ${esc(label)}</tspan></text>`);
    lx += 168;
}
y += LEGEND_H;

parts.push(`<rect x="${cardX}" y="${cardY}" width="${cardW}" height="${cardH}" rx="16" fill="${CARD}" stroke="${CARD_BORD}" stroke-width="1" filter="url(#cardShadow)"/>`);

const innerX = cardX + CARD_PAD;
const innerY = cardY + CARD_PAD;

const gridX = innerX + LOADER_COL + GUTTER;
const gridY = innerY + HEADER_H;

for (let c = 0; c < versions.length; c++) {
    const cx = gridX + c * (CELL_W + GAP_X) + CELL_W / 2;
    parts.push(`<text x="${cx}" y="${innerY + HEADER_H - 12}" font-size="12" font-weight="700" fill="${TEXT_DIM}" text-anchor="middle" letter-spacing="0.2">${esc(label(versions[c]))}</text>`);
}

for (let r = 0; r < loaders.length; r++) {
    const loader = loaders[r];
    const ry = gridY + r * (CELL_H + GAP_Y);

    parts.push(`<text x="${innerX + LOADER_COL - 16}" y="${ry + CELL_H / 2 + 5}" font-size="14" font-weight="700" fill="${TEXT_HI}" text-anchor="end">${esc(loader)}</text>`);

    for (let c = 0; c < versions.length; c++) {
        const v  = versions[c];
        const cx = gridX + c * (CELL_W + GAP_X);
        const status = statusOf(loader, v);
        const col = colorOf(status);
        parts.push(`<rect x="${cx}" y="${ry}" width="${CELL_W}" height="${CELL_H}" rx="10" fill="${col.bg}"/>`);
        parts.push(`<text x="${cx + CELL_W / 2}" y="${ry + CELL_H / 2 + 6}" font-size="17" font-weight="800" fill="${col.fg}" text-anchor="middle">${col.glyph}</text>`);
    }
}

parts.push('</svg>');

writeFileSync(outputFile, parts.join('\n') + '\n');
console.log(`Wrote ${outputFile} (${results.length} results, ${loaders.length}\u00d7${versions.length} matrix)`);