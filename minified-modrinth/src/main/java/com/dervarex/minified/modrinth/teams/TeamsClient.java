package com.dervarex.minified.modrinth.teams;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.internal.AbstractModrinthClient;
import org.apiguardian.api.API;

import java.util.List;
import java.util.Map;

/**
 * Gets the members of a team, get it from {@link Modrinth#teams()}. The team of a project is
 * {@link com.dervarex.minified.modrinth.projects.Project#getTeam()}
 */
public final class TeamsClient extends AbstractModrinthClient {
    @API(status = API.Status.INTERNAL)
    public TeamsClient(Modrinth modrinth) {
        super(modrinth);
    }

    /**
     * Gets the members of a team
     */
    public List<TeamMember> members(String teamId) {
        return getList("/team/" + encode(teamId) + "/members", Map.of(), TeamMember::new);
    }
}
