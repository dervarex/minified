/**
 * Public Modrinth user models and endpoints. Get the {@link com.dervarex.minified.modrinth.users.UsersClient}
 * from {@link com.dervarex.minified.modrinth.Modrinth#users()}:
 * <pre>{@code
 * User user = modrinth.users().get("dervarex");
 * List<User> users = modrinth.users().getMany("user-id-1", "user-id-2");
 * }</pre>
 * {@code get} takes a user ID or a username.
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.modrinth.users;

import org.apiguardian.api.API;