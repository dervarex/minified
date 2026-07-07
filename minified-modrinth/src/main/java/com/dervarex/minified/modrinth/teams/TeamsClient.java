package com.dervarex.minified.modrinth.teams;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.internal.AbstractModrinthClient;
import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.modrinth.users.User;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

import java.util.List;
import java.util.Map;

public final class TeamsClient extends AbstractModrinthClient {
    public TeamsClient(Modrinth modrinth) {
        super(modrinth);
    }

    public Team get(String id) {
        return parseTeam(getObject("/team/" + encode(id)));
    }

    public List<Team> getMany(String... ids) {
        Map<String, String> query = query("ids", toJsonArray(ids));
        return getList("/teams", query, this::parseTeam);
    }

    public List<TeamMember> members(String teamId) {
        JsonArray array = getArray("/team/" + encode(teamId) + "/members");
        return array.values().stream()
                .filter(value -> value != null && !value.isNull())
                .map(JsonValue::asObject)
                .map(this::parseTeamMember)
                .toList();
    }

    private Team parseTeam(JsonObject object) {
        if (object == null) {
            return null;
        }
        Team team = new Team();
        team.id = ModrinthJson.string(object, "id");
        team.name = ModrinthJson.string(object, "name");
        team.description = ModrinthJson.string(object, "description");
        team.iconUrl = ModrinthJson.string(object, "icon_url");
        team.url = ModrinthJson.string(object, "url");
        team.projects = ModrinthJson.strings(object, "projects");
        team.created = ModrinthJson.instant(object, "created");
        team.updated = ModrinthJson.instant(object, "updated");
        return team;
    }

    private TeamMember parseTeamMember(JsonObject object) {
        if (object == null) {
            return null;
        }
        TeamMember member = new TeamMember();
        JsonObject user = ModrinthJson.object(object, "user");
        if (user != null) {
            member.user = parseUser(user);
        }
        member.role = ModrinthJson.string(object, "role");
        member.permissions = ModrinthJson.strings(object, "permissions");
        member.joined = ModrinthJson.instant(object, "joined");
        return member;
    }

    private User parseUser(JsonObject object) {
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

