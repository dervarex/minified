/**
 * The teams behind projects. Get the {@link com.dervarex.minified.modrinth.teams.TeamsClient}
 * from {@link com.dervarex.minified.modrinth.Modrinth#teams()}:
 * <pre>{@code
 * Project project = modrinth.projects().get("sodium");
 * List<TeamMember> members = modrinth.teams().members(project.getTeam());
 * }</pre>
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.modrinth.teams;

import org.apiguardian.api.API;