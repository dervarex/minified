package com.dervarex.minified.modrinth.tags;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.internal.AbstractModrinthClient;
import com.dervarex.minified.modrinth.projects.ProjectType;
import com.dervarex.minified.utils.json.JsonValue;
import org.apiguardian.api.API;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Gets the lists Modrinth uses to describe projects (called Tags), like categories, loaders and game versions
 */
public final class TagsClient extends AbstractModrinthClient {
    @API(status = API.Status.INTERNAL)
    public TagsClient(Modrinth modrinth) {
        super(modrinth);
    }

    /** {@return all project categories} */
    public List<Category> categories() {
        return getList("/tag/category", Map.of(), Category::new);
    }

    /** {@return all loaders} */
    public List<LoaderTag> loaders() {
        return getList("/tag/loader", Map.of(), LoaderTag::new);
    }

    /** {@return all Minecraft versions, newest first} */
    public List<GameVersion> gameVersions() {
        return getList("/tag/game_version", Map.of(), GameVersion::new);
    }

    /** {@return all donation platforms} */
    public List<DonationPlatform> donationPlatforms() {
        return getList("/tag/donation_platform", Map.of(), DonationPlatform::new);
    }

    /** {@return all project types} */
    public List<ProjectType> projectTypes() {
        return getArray("/tag/project_type").values().stream()
                .filter(value -> value != null && !value.isNull())
                .map(JsonValue::asString)
                .map(ProjectType::fromApiValue)
                .filter(Objects::nonNull)
                .toList();
    }
}
