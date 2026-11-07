package com.dervarex.minified.utils.version;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonValue;

import java.io.IOException;

/**
 * Reads single values from a version's JSON <br>
 * Every method returns null if the version doesn't exist
 */
public final class VersionMetadataProvider {
    private VersionMetadataProvider() {
    }

    /**
     * {@return the url of the version JSON}
     *
     * @param version the version id
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     */
    public static String getVersionJsonUrl(String version) throws HttpException, IOException {
        JsonValue entry = VersionManifestClient.getVersionEntry(version);
        if (entry == null) {
            return null;
        }
        return entry.asObject().get("url").asString();
    }

    /**
     * {@return the main class}
     *
     * @param version the version id
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     */
    public static String getMainClass(String version) throws HttpException, IOException {
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }
        JsonFile json = new JsonFile(VersionManifestClient.CACHE.get(url));
        return json.get("mainClass").asString();
    }
    /**
     * {@return the type, like release or snapshot}
     *
     * @param version the version id
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     */
    public static String getVersionType(String version) throws HttpException, IOException {
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }
        JsonFile json = new JsonFile(VersionManifestClient.CACHE.get(url));
        return json.get("type").asString();
    }
    /**
     * Returns the release time as an <a href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a> string
     * @param version the version id
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     * @return the release time as an ISO 8601 string
     */
    public static String getReleaseTime(String version) throws HttpException, IOException {
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }
        JsonFile json = new JsonFile(VersionManifestClient.CACHE.get(url));
        return json.get("releaseTime").asString();
    }
    /**
     * {@return the SHA-1 of the client jar}
     *
     * @param version the version id
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     */
    public static String getClientSha1(String version) throws HttpException, IOException {
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }

        JsonFile json = new JsonFile(VersionManifestClient.CACHE.get(url));

        return json.get("downloads")
                .asObject()
                .get("client")
                .asObject()
                .get("sha1")
                .asString();
    }
    /**
     * {@return the download url of the client jar}
     *
     * @param version the version id
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     */
    public static String getClientUrl(String version)
            throws HttpException, IOException {

        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }

        JsonFile json = new JsonFile(VersionManifestClient.CACHE.get(url));

        return json.get("downloads")
                .asObject()
                .get("client")
                .asObject()
                .get("url")
                .asString();
    }
    /**
     * {@return the SHA-1 of the server jar}
     *
     * @param version the version id
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     */
    public static String getServerSha1(String version)
            throws HttpException, IOException {
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }
        JsonFile json = new JsonFile(VersionManifestClient.CACHE.get(url));
        return json.get("downloads")
                .asObject()
                .get("server")
                .asObject()
                .get("sha1")
                .asString();
    }

    /**
     * {@return the download url of the server jar}
     *
     * @param version the version id
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     */
    public static String getServerUrl(String version)
            throws HttpException, IOException {
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }
        JsonFile json = new JsonFile(VersionManifestClient.CACHE.get(url));
        return json.get("downloads")
                .asObject()
                .get("server")
                .asObject()
                .get("url")
                .asString();
    }
}