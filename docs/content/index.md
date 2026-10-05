---
title: ​
description: A simple Java library for building Minecraft launchers.
tableOfContents: false

---

<section class="home-wrap">
	<div class="home-copy">
		<p class="home-kicker">Java library for Minecraft launchers</p>

		<h2>Keep launcher code simple</h2>

		<p>
			Minified handles authentication, versions, assets, libraries, Java runtimes
			and mod loaders behind a clean, modular API.
		</p>
	</div>

	<div class="home-code-card" aria-label="Minified example">
		<div class="home-code-header">
			<span>Example</span>
			<span>Java</span>
		</div>

		<div class="home-code">
```java
LaunchConfiguration config = new LaunchConfiguration.Builder()
    .assetsDirectory(Path.of("<your assets directory>"))
    .librariesDirectory(Path.of("<your libraries directory>"))
    .jarFile(Path.of("<path to your client.jar>"))
    .loader(new VanillaLoader("26.2"))
    .build();

Launcher.launchMinecraft(
    null, // Use the authentication guide to pass a user.
    config
);
```
	</div>
</div>

	<nav class="home-links" aria-label="Start here">
		<a class="home-link" href="/guides/getting-started">
			<span><small>START HERE</small><strong>Getting Started</strong></span>
			<span class="home-arrow">→</span>
		</a>
		<a class="home-link" href="/events/introduction">
			<span><small>REACT TO THE LAUNCH</small><strong>Events</strong></span>
			<span class="home-arrow">→</span>
		</a>
		<a class="home-link" href="/api">
			<span><small>EVERY CLASS</small><strong>API Reference</strong></span>
			<span class="home-arrow">→</span>
		</a>
	</nav>
</section>
