package com.dervarex.minified.launch.launch.modding.neoforge.api;

import com.dervarex.minified.launch.exceptions.loader.NoLoadersFoundException;
import com.dervarex.minified.launch.exceptions.loader.neoforge.FailedToReadMetadataException;
import com.dervarex.minified.launch.exceptions.loader.neoforge.MalformedMetadataException;
import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import com.dervarex.minified.utils.json.JsonValue;
import org.apiguardian.api.API;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NeoVersionFetcher {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final AtomicReference<List<String>> cachedVersions = new AtomicReference<>();
    private final AtomicReference<List<String>> cachedDirectoryVersions = new AtomicReference<>();

    @API(status = API.Status.STABLE)
    public String getLatest(String minecraftVersion) {
        return resolveLoaderVersion(minecraftVersion);
    }

    @API(status = API.Status.STABLE)
    public String resolveLoaderVersion(String versionOrMinecraftVersion) {
        String resolved = resolveFrom(getAllVersions(), versionOrMinecraftVersion);
        if (resolved == null) {
            // NeoForge sometimes regenerates maven-metadata.xml (and the version API with it) with only the newest
            // versions in it, the directory listing still has all of them
            resolved = resolveFrom(getDirectoryVersions(), versionOrMinecraftVersion);
        }
        if (resolved != null) {
            return resolved;
        }

        throw new NoLoadersFoundException(
                "No NeoForge version found for " + versionOrMinecraftVersion,
                "NEOFORGE" // todo replace that uppercase string with an enum
        );
    }

    private static String resolveFrom(List<String> versions, String versionOrMinecraftVersion) {
        if (versions.contains(versionOrMinecraftVersion)) {
            return versionOrMinecraftVersion;
        }
        return versions.stream()
                .filter(version -> matchesMinecraftBranch(version, versionOrMinecraftVersion))
                .max(VERSION_ORDER)
                .orElse(null);
    }

    @API(status = API.Status.STABLE)
    public List<String> getAllVersions() {
        List<String> cached = cachedVersions.get();
        if (cached != null) {
            return cached;
        }

        List<String> loaded = fetchVersions();
        cachedVersions.compareAndSet(null, loaded);
        return Objects.requireNonNullElse(cachedVersions.get(), loaded);
    }

    private List<String> fetchVersions() {
        List<String> versions = new ArrayList<>(
                fetchVersions(ApiEndpoints.NEOFORGE_MAVEN_METADATA_URL, ApiEndpoints.NEOFORGE_VERSIONS_API_URL)
        );
        versions.addAll(
                fetchVersions(ApiEndpoints.NEOFORGE_LEGACY_MAVEN_METADATA_URL, ApiEndpoints.NEOFORGE_LEGACY_VERSIONS_API_URL)
        );
        return List.copyOf(versions);
    }

    private List<String> getDirectoryVersions() {
        List<String> cached = cachedDirectoryVersions.get();
        if (cached != null) {
            return cached;
        }

        List<String> versions = new ArrayList<>(fetchVersionsFromDirectory(ApiEndpoints.NEOFORGE_DIRECTORY_API_URL));
        versions.addAll(fetchVersionsFromDirectory(ApiEndpoints.NEOFORGE_LEGACY_DIRECTORY_API_URL));
        cachedDirectoryVersions.compareAndSet(null, List.copyOf(versions));
        return cachedDirectoryVersions.get();
    }

    private List<String> fetchVersionsFromDirectory(String directoryUrl) {
        try {
            JsonValue files = JsonParser.parse(fetch(directoryUrl)).asObject().get("files");
            if (files == null) {
                throw new MalformedMetadataException("NeoForge directory listing did not contain any files: " + directoryUrl);
            }

            List<String> versions = new ArrayList<>();
            for (JsonValue file : files.asArray()) {
                JsonObject entry = file.asObject();
                if ("DIRECTORY".equals(entry.getString("type"))) {
                    versions.add(entry.getString("name"));
                }
            }
            return versions;
        } catch (Exception e) {
            throw new FailedToReadMetadataException("Failed to read NeoForge directory listing from " + directoryUrl, e);
        }
    }

    private List<String> fetchVersions(String metadataUrl, String apiUrl) {
        try {
            return fetchVersionsFromMetadata(metadataUrl);
        } catch (FailedToReadMetadataException metadataException) {
            // the NeoForge CDN sometimes for some reason serves a cached 404 for the metadata file for about a minute
            try {
                return fetchVersionsFromApi(apiUrl);
            } catch (FailedToReadMetadataException apiException) {
                apiException.addSuppressed(metadataException);
                throw apiException;
            }
        }
    }

    private List<String> fetchVersionsFromApi(String apiUrl) {
        try {
            JsonValue versionsValue = JsonParser.parse(fetch(apiUrl)).asObject().get("versions");
            if (versionsValue == null || versionsValue.asArray().size() == 0) {
                throw new MalformedMetadataException("NeoForge version API did not return any versions: " + apiUrl);
            }

            List<String> versions = new ArrayList<>();
            for (JsonValue version : versionsValue.asArray()) {
                versions.add(version.asString());
            }
            return versions;
        } catch (Exception e) {
            throw new FailedToReadMetadataException("Failed to read NeoForge versions from " + apiUrl, e);
        }
    }

    private static String fetch(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new FailedToReadMetadataException("Failed to fetch " + url + ": HTTP " + response.statusCode());
        }
        return response.body();
    }

    private List<String> fetchVersionsFromMetadata(String metadataUrl) {
        try {
            String body = fetch(metadataUrl);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(body)));
            NodeList nodes = document.getElementsByTagName("version");
            List<String> versions = new ArrayList<>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                String value = nodes.item(i).getTextContent();
                if (value != null && !value.isBlank()) {
                    versions.add(value.trim());
                }
            }
            if (versions.isEmpty()) {
                throw new MalformedMetadataException("NeoForge metadata did not contain any versions: " + metadataUrl);
            }
            return versions;
        } catch (Exception e) {
            throw new FailedToReadMetadataException("Failed to read NeoForge metadata from " + metadataUrl, e);
        }
    }

    private static boolean matchesMinecraftBranch(String neoForgeVersion, String minecraftVersion) {
        return neoForgeVersion.startsWith(minecraftVersion + "-") // legacy 1.20.1 versions, e.g. 1.20.1-47.1.106
                || neoForgeVersion.startsWith(toNeoForgeVersionPrefix(minecraftVersion));
    }

    /**
     * NeoForge versions encode the Minecraft version without the leading "1.", e.g. 1.21.1 -> 21.1.x and 1.21 -> 21.0.x.
     * Since Minecraft 26.1 they contain the full version, e.g. 26.1 -> 26.1.0.x and 26.1.2 -> 26.1.2.x.
     *
     * @param minecraftVersion the Minecraft version, for example {@code 1.21.1}
     * @return the prefix all NeoForge versions for that Minecraft version start with, for example {@code 21.1.}
     */
    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public static String toNeoForgeVersionPrefix(String minecraftVersion) {
        List<String> parts = new ArrayList<>(List.of(minecraftVersion.split("\\.")));
        int length = 3;
        if (parts.getFirst().equals("1")) {
            parts.removeFirst();
            length = 2;
        }
        while (parts.size() < length) {
            parts.add("0");
        }
        return String.join(".", parts) + ".";
    }

    /**
     * @param neoForgeVersion a NeoForge version
     * @return true if the version was published as {@code net.neoforged:forge} (only used for Minecraft 1.20.1)
     */
    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public static boolean isLegacyVersion(String neoForgeVersion) {
        return neoForgeVersion.startsWith("1.");
    }

    private static final Comparator<String> VERSION_ORDER = NeoVersionFetcher::compareVersions;

    // package-private for the tests
    static int compareVersions(String left, String right) {
        List<Token> a = tokenize(left);
        List<Token> b = tokenize(right);
        int max = Math.max(a.size(), b.size());

        for (int i = 0; i < max; i++) {
            // one ran out: more numbers means newer (26.1.0.20 > 26.1.0), only text means pre-release (21.4.0-beta < 21.4.0)
            if (i >= a.size()) {
                return hasNumberFrom(b, i) ? -1 : 1;
            }
            if (i >= b.size()) {
                return hasNumberFrom(a, i) ? 1 : -1;
            }

            Token ta = a.get(i);
            Token tb = b.get(i);
            int cmp = ta.compareTo(tb);
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0;
    }

    private static boolean hasNumberFrom(List<Token> tokens, int start) {
        for (int i = start; i < tokens.size(); i++) {
            if (tokens.get(i).kind == Kind.NUMBER) {
                return true;
            }
        }
        return false;
    }

    private static List<Token> tokenize(String version) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\d+|[A-Za-z]+").matcher(version);
        while (matcher.find()) {
            String value = matcher.group();
            if (value == null || value.isBlank()) {
                continue;
            }
            if (Character.isDigit(value.charAt(0))) {
                tokens.add(Token.number(value));
            } else {
                tokens.add(Token.text(value));
            }
        }
        return tokens;
    }

    private enum Kind {
        NUMBER,
        TEXT
    }

    private record Token(Kind kind, String value) implements Comparable<Token> {
        static Token number(String value) {
            return new Token(Kind.NUMBER, value);
        }

        static Token text(String value) {
            return new Token(Kind.TEXT, value.toLowerCase());
        }

        @Override
        public int compareTo(Token other) {
            if (kind != other.kind) {
                return kind == Kind.NUMBER ? 1 : -1;
            }
            if (kind == Kind.NUMBER) {
                return new BigInteger(value).compareTo(new BigInteger(other.value));
            }
            return value.compareTo(other.value);
        }
    }
}
