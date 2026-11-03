/**
 * Public Modrinth team models and endpoints. Get the {@link com.dervarex.minified.modrinth.teams.TeamsClient}
 * from {@link com.dervarex.minified.modrinth.Modrinth#teams()}:
 * <pre>{@code
 * Team team = modrinth.teams().get("team-id");
 * List<Team> teams = modrinth.teams().getMany("team-id-1", "team-id-2");
 * List<TeamMember> members = modrinth.teams().members("team-id");
 * }</pre>
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.modrinth.teams;

import org.apiguardian.api.API;