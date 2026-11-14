package com.dervarex.minified.modrinth.teams;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.utils.json.JsonParser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamsClientTest {

    @Test
    void parsesMembers() {
        TeamMember member = new TeamMember(JsonParser.parse("""
                {"team_id":"4reLOAKe","role":"Maintainer","ordering":2,
                 "user":{"id":"DzLrfrbK","username":"IMS","avatar_url":"https://avatar","bio":null,"role":"developer"}}""")
                .asObject());

        assertEquals("4reLOAKe", member.getTeamId());
        assertEquals("Maintainer", member.getRole());
        assertEquals(2, member.getOrdering());
        assertEquals("IMS", member.getUser().getUsername());
        assertEquals("developer", member.getUser().getRole());
    }

    @Test
    @Tag("manual")
    void sodiumsTeamHasMembers() {
        Modrinth modrinth = Modrinth.connect();
        List<TeamMember> members = modrinth.teams().members(modrinth.projects().get("sodium").getTeam());

        assertFalse(members.isEmpty());
        assertTrue(members.stream().allMatch(member -> member.getUser().getId() != null));
    }
}
