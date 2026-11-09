package com.dervarex.minified.launch.profile;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Describes a profile for people, none of it is needed to launch the game
 *
 * @param id          a unique id that stays the same when the profile is shared, a random UUID by default
 * @param name        the display name, can be null
 * @param description a longer description, can be null
 * @param author      who made the profile, can be null
 * @param iconUrl     a direct link to an icon, can be null
 * @param createdAt   when the profile was created, can be null
 * @param updatedAt   when the profile was last changed, can be null
 */
@API(status = API.Status.EXPERIMENTAL)
public record ProfileMetadata(
        String id,
        String name,
        String description,
        String author,
        String iconUrl,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Checks that the id is set
     */
    public ProfileMetadata {
        Objects.requireNonNull(id, "id");
    }

    /**
     * {@return a copy with {@code updatedAt} set to now}
     */
    public ProfileMetadata touch() {
        return new ProfileMetadata(id, name, description, author, iconUrl, createdAt, Instant.now());
    }

    /**
     * {@return a builder with a random id, created and updated now}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@return a builder with the values of this metadata}
     */
    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .name(name)
                .description(description)
                .author(author)
                .iconUrl(iconUrl)
                .createdAt(createdAt)
                .updatedAt(updatedAt);
    }

    /**
     * Builds a {@link ProfileMetadata}
     */
    public static class Builder {
        private String id = UUID.randomUUID().toString();
        private String name;
        private String description;
        private String author;
        private String iconUrl;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = createdAt;

        /**
         * Sets the id, a random UUID by default
         *
         * @param id the id
         * @return this builder
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the display name
         *
         * @param name the name, can be null
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the description
         *
         * @param description the description, can be null
         * @return this builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets who made the profile
         *
         * @param author the author, can be null
         * @return this builder
         */
        public Builder author(String author) {
            this.author = author;
            return this;
        }

        /**
         * Sets the icon
         *
         * @param iconUrl a direct link to the icon, can be null
         * @return this builder
         */
        public Builder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        /**
         * Sets when the profile was created, now by default
         *
         * @param createdAt the time, can be null
         * @return this builder
         */
        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * Sets when the profile was last changed, now by default
         *
         * @param updatedAt the time, can be null
         * @return this builder
         */
        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * {@return the metadata}
         */
        public ProfileMetadata build() {
            return new ProfileMetadata(id, name, description, author, iconUrl, createdAt, updatedAt);
        }
    }
}
