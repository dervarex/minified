package com.dervarex.minified.modrinth.users;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.internal.AbstractModrinthClient;
import org.apiguardian.api.API;

import java.util.List;

/**
 * Gets users by ID or username, get it from {@link Modrinth#users()}
 */
public final class UsersClient extends AbstractModrinthClient {
    /**
     * Creates the client, use the one from {@link Modrinth}
     *
     * @param modrinth the Modrinth it belongs to
     */
    @API(status = API.Status.INTERNAL)
    public UsersClient(Modrinth modrinth) {
        super(modrinth);
    }

    /**
     * Gets a user by their ID or username
     *
     * @param idOrUsername the ID or username
     * @return the user
     * @throws com.dervarex.minified.modrinth.exceptions.ModrinthNotFoundException if there is no such user
     */
    public User get(String idOrUsername) {
        return get("/user/" + encode(idOrUsername), User::new);
    }

    /**
     * Gets several users by their IDs or usernames.
     *
     * @param idsOrUsernames the IDs or usernames
     * @return the users, ones that don't exist are left out
     */
    public List<User> getMany(String... idsOrUsernames) {
        if (idsOrUsernames == null || idsOrUsernames.length == 0) {
            return List.of();
        }
        return getList("/users", query("ids", toJsonArray(idsOrUsernames)), User::new);
    }
}
