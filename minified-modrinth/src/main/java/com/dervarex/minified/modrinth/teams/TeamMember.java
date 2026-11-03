package com.dervarex.minified.modrinth.teams;

import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.modrinth.users.User;
import com.dervarex.minified.utils.json.JsonObject;

/**
 * A user in the team behind a project
 */
public final class TeamMember {
    private final String teamId;
    private final User user;
    private final String role;
    private final int ordering;

    TeamMember(JsonObject json) {
        teamId = ModrinthJson.string(json, "team_id");
        JsonObject userJson = ModrinthJson.object(json, "user");
        user = userJson == null ? null : new User(userJson);
        role = ModrinthJson.string(json, "role");
        ordering = (int) ModrinthJson.number(json, "ordering");
    }

    /** {@return the ID of the team} */
    public String getTeamId() {
        return teamId;
    }

    /** {@return the user} */
    public User getUser() {
        return user;
    }

    /** {@return the role in the team, like {@code Owner}, written by the team itself} */
    public String getRole() {
        return role;
    }

    /** {@return where the member is shown in the team, lower ones come first} */
    public int getOrdering() {
        return ordering;
    }

    /**
     * Describes the member, for logging purposes
     *
     * @return the user and role
     */
    @Override
    public String toString() {
        return "TeamMember{" + user + ", " + role + "}";
    }
}
