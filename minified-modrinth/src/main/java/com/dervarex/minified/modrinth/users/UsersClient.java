package com.dervarex.minified.modrinth.users;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.internal.AbstractModrinthClient;
import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UsersClient extends AbstractModrinthClient {
    public UsersClient(Modrinth modrinth) {
        super(modrinth);
    }

    public User get(String idOrUsername) {
        return parseUser(getObject("/user/" + encode(idOrUsername)));
    }

    public List<User> getMany(String... ids) {
        Map<String, String> query = query("ids", toJsonArray(ids));
        return getList("/users", query, this::parseUser);
    }

    private User parseUser(JsonObject object) {
        if (object == null) {
            return null;
        }
        User user = new User();
        user.id = ModrinthJson.string(object, "id");
        user.username = ModrinthJson.string(object, "username");
        user.name = ModrinthJson.string(object, "name");
        user.avatarUrl = ModrinthJson.string(object, "avatar_url");
        user.bio = ModrinthJson.string(object, "bio");
        user.created = ModrinthJson.instant(object, "created");
        user.role = ModrinthJson.string(object, "role");
        return user;
    }

    private static String toJsonArray(String... values) {
        if (values == null || values.length == 0) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(value.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        builder.append(']');
        return builder.toString();
    }
}

