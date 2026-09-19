package com.dervarex.minified.modrinth.teams;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TeamsClientTest {

    private static TeamsClient newClient() {
        return new TeamsClient(new Modrinth("http://test"));
    }

    private static Team parseTeam(JsonObject object) throws Exception {
        Method method = TeamsClient.class.getDeclaredMethod("parseTeam", JsonObject.class);
        method.setAccessible(true);
        return (Team) method.invoke(newClient(), object);
    }

    private static TeamMember parseTeamMember(JsonObject object) throws Exception {
        Method method = TeamsClient.class.getDeclaredMethod("parseTeamMember", JsonObject.class);
        method.setAccessible(true);
        return (TeamMember) method.invoke(newClient(), object);
    }

    private static String toJsonArray(String... values) throws Exception {
        Method method = TeamsClient.class.getDeclaredMethod("toJsonArray", String[].class);
        method.setAccessible(true);
        return (String) method.invoke(null, (Object) values);
    }

    @Test
    void parseTeam_parsesAllFields() throws Exception {
        JsonObject obj = new JsonObject();
        obj.put("id", "team1");
        obj.put("name", "My Team");
        obj.put("description", "A team");
        obj.put("icon_url", "icon.png");
        obj.put("url", "https://example.com/team");
        obj.put("created", "2024-01-01T00:00:00Z");
        obj.put("updated", "2024-02-01T00:00:00Z");

        Team team = parseTeam(obj);
        assertEquals("team1", team.getId());
        assertEquals("My Team", team.getName());
        assertEquals("A team", team.getDescription());
        assertEquals("icon.png", team.getIconUrl());
        assertEquals("https://example.com/team", team.getUrl());
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), team.getCreated());
        assertEquals(Instant.parse("2024-02-01T00:00:00Z"), team.getUpdated());
    }

    @Test
    void parseTeam_parsesProjects() throws Exception {
        JsonObject obj = new JsonObject();
        JsonArray projects = new JsonArray();
        projects.add("p1");
        projects.add("p2");
        obj.put("projects", projects);

        assertArrayEquals(new String[]{"p1", "p2"}, parseTeam(obj).getProjects());
    }

    @Test
    void parseTeam_returnsNull_whenObjectNull() throws Exception {
        assertNull(parseTeam(null));
    }

    @Test
    void parseTeamMember_parsesBasicFields() throws Exception {
        JsonObject obj = new JsonObject();
        obj.put("role", "owner");
        obj.put("joined", "2024-03-01T00:00:00Z");

        TeamMember member = parseTeamMember(obj);
        assertEquals("owner", member.getRole());
        assertEquals(Instant.parse("2024-03-01T00:00:00Z"), member.getJoined());
    }

    @Test
    void parseTeamMember_parsesPermissions() throws Exception {
        JsonObject obj = new JsonObject();
        JsonArray perms = new JsonArray();
        perms.add("read");
        perms.add("write");
        obj.put("permissions", perms);

        assertArrayEquals(new String[]{"read", "write"}, parseTeamMember(obj).getPermissions());
    }

    @Test
    void parseTeamMember_parsesUser() throws Exception {
        JsonObject userObj = new JsonObject();
        userObj.put("id", "u1");
        userObj.put("username", "alice");
        userObj.put("name", "Alice");
        userObj.put("avatar_url", "a.png");
        userObj.put("bio", "hello");
        userObj.put("created", "2024-01-01T00:00:00Z");
        userObj.put("role", "admin");
        JsonObject obj = new JsonObject();
        obj.put("user", userObj);

        TeamMember member = parseTeamMember(obj);
        assertNotNull(member.getUser());
        assertEquals("u1", member.getUser().getId());
        assertEquals("alice", member.getUser().getUsername());
        assertEquals("Alice", member.getUser().getName());
        assertEquals("a.png", member.getUser().getAvatarUrl());
        assertEquals("hello", member.getUser().getBio());
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), member.getUser().getCreated());
        assertEquals("admin", member.getUser().getRole());
    }

    @Test
    void parseTeamMember_userNull_whenMissing() throws Exception {
        JsonObject obj = new JsonObject();
        assertNull(parseTeamMember(obj).getUser());
    }

    @Test
    void parseTeamMember_returnsNull_whenObjectNull() throws Exception {
        assertNull(parseTeamMember(null));
    }

    @Test
    void toJsonArray_buildsArray() throws Exception {
        assertEquals("[\"a\",\"b\"]", toJsonArray("a", "b"));
    }

    @Test
    void toJsonArray_returnsEmptyArrayForNoValues() throws Exception {
        assertEquals("[]", toJsonArray());
    }

    @Test
    void toJsonArray_skipsNullOrBlank() throws Exception {
        assertEquals("[\"a\"]", toJsonArray("a", null, ""));
    }

    @Test
    void toJsonArray_escapesQuotesAndBackslashes() throws Exception {
        assertEquals("[\"a\\\"b\",\"c\\\\d\"]", toJsonArray("a\"b", "c\\d"));
    }
}