/**
 * Client for the public Modrinth API
 * <p>
 * <h2>Connecting</h2>
 * Create a {@link com.dervarex.minified.modrinth.Modrinth} with {@link com.dervarex.minified.modrinth.Modrinth#connect()}:
 * <pre>{@code
 * Modrinth modrinth = Modrinth.connect();
 * }</pre>
 * This talks to the public Modrinth API at {@code https://api.modrinth.com/v2}. For a staging instance or a
 * self-hosted, API-compatible mirror, pass a custom base URL instead:
 * <pre>{@code
 * Modrinth modrinth = Modrinth.connect("https://staging-api.modrinth.com/v2");
 * }</pre>
 * A {@link Modrinth} is cheap to keep, feel free to reuse it anytime, its clients are created once and shared anyway.
 *
 * <h2>Clients</h2>
 * <ul>
 *     <li>{@link com.dervarex.minified.modrinth.Modrinth#projects()}: fetch, search and inspect projects</li>
 *     <li>{@link com.dervarex.minified.modrinth.Modrinth#versions()}: versions, their files and dependencies</li>
 *     <li>{@link com.dervarex.minified.modrinth.Modrinth#tags()}: categories, loaders, licenses and other search
 *     metadata</li>
 *     <li>{@link com.dervarex.minified.modrinth.Modrinth#teams()} and
 *     {@link com.dervarex.minified.modrinth.Modrinth#users()}: who owns a project</li>
 * </ul>
 * <br>
 * Exceptions are in {@link com.dervarex.minified.modrinth.exceptions}, and the loaders Modrinth
 * knows are in {@link com.dervarex.minified.modrinth.loaders}
 */
package com.dervarex.minified.modrinth;
