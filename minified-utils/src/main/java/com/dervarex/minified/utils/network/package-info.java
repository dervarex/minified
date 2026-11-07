/**
 * Network connectivity and host reachability helpers.
 * Has to be checked for functionality, please be careful around here.
 * <br><br>
 * {@link com.dervarex.minified.utils.network.NetworkUtil#ensureOnline(String)} runs up to three checks, one after the
 * other until one works:
 * <ol>
 *     <li>DNS: resolves example.com</li>
 *     <li>TCP: connects to 1.1.1.1:53, 1.2 second timeout</li>
 *     <li>HTTP: a HEAD request to https://api.mojang.com, 1.5 second timeout, any status below 500 counts</li>
 * </ol>
 * Only if all three fail, it throws a {@link com.dervarex.minified.utils.exceptions.NoConnectionException}
 * <pre>{@code
 * try {
 *     NetworkUtil.ensureOnline("download client jar");
 * } catch (NoConnectionException e) {
 *     System.out.println(e.toUserFriendlyMessage());
 * }
 * }</pre>
 * The action ends up in the exception, so pass what you were trying to do, like "download client jar" or "login" <br>
 * It doesn't post any events, you can fire {@code CheckConnectionEvent} and {@code OfflineEvent} yourself if you want to
 * <br><br>
 * {@link com.dervarex.minified.utils.exceptions.NoConnectionException#throwIfOffline throwIfOffline} gives you the same
 * exception for your own check, without running the three checks above
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.utils.network;

import org.apiguardian.api.API;
