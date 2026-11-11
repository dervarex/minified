/**
 * Downloading files with SHA-1 checks and progress
 * <p>
 * Start with {@link com.dervarex.minified.utils.download.DownloadHelper}, it's pretty much self-explanatory
 *
 * <h2>Downloading</h2>
 * <pre>{@code
 * boolean ok = DownloadHelper.download(url, targetPath, expectedSha1);
 * }</pre>
 * A file that already exists with the right SHA-1 gets skipped, one with a different SHA-1 gets downloaded again <br>
 * If the downloaded file still has the wrong SHA-1, it's thrown away and you get false instead of an exception
 *
 * <h2>Progress</h2>
 * <pre>{@code
 * AtomicLong downloaded = new AtomicLong();
 * DownloadHelper.download(url, targetPath, expectedSha1, httpClient, bytes -> {
 *     long total = downloaded.addAndGet(bytes);
 *     System.out.println("Downloaded " + total + " bytes so far");
 * });
 * }</pre>
 * SHA-1 of files you already have: {@link com.dervarex.minified.utils.sha.Hasher#sha1(java.nio.file.Path)}
 */
package com.dervarex.minified.utils.download;
