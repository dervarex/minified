package com.dervarex.minified.modrinth.tags;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.projects.ProjectType;
import com.dervarex.minified.modrinth.projects.SideSupport;
import com.dervarex.minified.modrinth.internal.AbstractModrinthClient;
import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

import java.util.List;

public final class TagsClient extends AbstractModrinthClient {
    public TagsClient(Modrinth modrinth) {
        super(modrinth);
    }

    public List<Tag> categories() {
        return fetchTags("/tag/category");
    }

    public List<Tag> loaders() {
        return fetchTags("/tag/loader");
    }

    public List<Tag> versions() {
        return fetchTags("/tag/version");
    }

    public List<Tag> users() {
        return fetchTags("/tag/user");
    }

    public List<Tag> teams() {
        return fetchTags("/tag/team");
    }

    public List<Tag> versionFiles() {
        return fetchTags("/tag/version_file");
    }

    public List<Tag> tags() {
        return fetchTags("/tag/tag");
    }

    public List<License> licenses() {
        JsonArray array = getArray("/tag/license");
        return array.values().stream()
                .filter(value -> value != null && !value.isNull())
                .map(JsonValue::asObject)
                .map(this::parseLicense)
                .toList();
    }

    public List<ProjectType> projectTypes() {
        JsonArray array = getArray("/tag/project_type");
        return array.values().stream()
                .filter(value -> value != null && !value.isNull())
                .map(JsonValue::asString)
                .map(ProjectType::fromApiValue)
                .toList();
    }

    public List<SideSupport> sideTypes() {
        JsonArray array = getArray("/tag/side_type");
        return array.values().stream()
                .filter(value -> value != null && !value.isNull())
                .map(JsonValue::asString)
                .map(SideSupport::fromApiValue)
                .toList();
    }

    public List<Tag> donationPlatforms() {
        return fetchTags("/tag/donation_platform");
    }

    private List<Tag> fetchTags(String path) {
        JsonArray array = getArray(path);
        return array.values().stream()
                .filter(value -> value != null && !value.isNull())
                .map(JsonValue::asObject)
                .map(this::parseTag)
                .toList();
    }

    private Tag parseTag(JsonObject object) {
        if (object == null) {
            return null;
        }
        Tag tag = new Tag();
        tag.id = ModrinthJson.string(object, "id");
        tag.name = ModrinthJson.string(object, "name");
        tag.description = ModrinthJson.string(object, "description");
        tag.icon = ModrinthJson.string(object, "icon");
        tag.header = ModrinthJson.string(object, "header");
        tag.value = ModrinthJson.string(object, "value");
        Boolean featured = ModrinthJson.bool(object, "featured");
        tag.featured = featured != null && featured;
        tag.projectType = ProjectType.fromApiValue(ModrinthJson.string(object, "project_type"));
        tag.projectTypes = ModrinthJson.strings(object, "project_types");
        return tag;
    }

    private License parseLicense(JsonObject object) {
        if (object == null) {
            return null;
        }
        License license = new License();
        license.id = ModrinthJson.string(object, "id");
        license.name = ModrinthJson.string(object, "name");
        license.url = ModrinthJson.string(object, "url");
        return license;
    }
}

