package com.dervarex.minified.launch.launch.modding.neoforge.api;

import com.dervarex.minified.launch.exceptions.loader.neoforge.FailedToReadMetadataException;
import com.dervarex.minified.launch.exceptions.loader.neoforge.MalformedMetadataException;
import com.dervarex.minified.utils.ApiEndpoints;
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

    public String getLatest(String minecraftVersion) {
        return resolveLoaderVersion(minecraftVersion);
    }

    public String resolveLoaderVersion(String versionOrMinecraftVersion) {
        List<String> allVersions = getAllVersions();

        if (allVersions.contains(versionOrMinecraftVersion)) {
            return versionOrMinecraftVersion;
        }

        List<String> matchingBranch = allVersions.stream()
                .filter(version -> matchesMinecraftBranch(version, versionOrMinecraftVersion))
                .sorted(VERSION_ORDER.reversed())
                .toList();

        if (!matchingBranch.isEmpty()) {
            return matchingBranch.get(0);
        }

        return allVersions.stream()
                .sorted(VERSION_ORDER.reversed())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No NeoForge versions found in metadata"));
    }

    private List<String> getAllVersions() {
        List<String> cached = cachedVersions.get();
        if (cached != null) {
            return cached;
        }

        List<String> loaded = fetchVersions();
        cachedVersions.compareAndSet(null, loaded);
        return Objects.requireNonNullElseGet(cachedVersions.get(), () -> loaded);
    }

    private List<String> fetchVersions() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiEndpoints.NEOFORGE_MAVEN_METADATA_URL))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new FailedToReadMetadataException("Failed to fetch NeoForge metadata: HTTP " + response.statusCode());
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(response.body())));
            NodeList nodes = document.getElementsByTagName("version");
            List<String> versions = new ArrayList<>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                String value = nodes.item(i).getTextContent();
                if (value != null && !value.isBlank()) {
                    versions.add(value.trim());
                }
            }
            if (versions.isEmpty()) {
                throw new MalformedMetadataException("NeoForge metadata did not contain any versions");
            }
            return List.copyOf(versions);
        } catch (Exception e) {
            throw new FailedToReadMetadataException("Failed to read NeoForge metadata", e);
        }
    }

    private static boolean matchesMinecraftBranch(String neoForgeVersion, String minecraftVersion) {
        return neoForgeVersion.equals(minecraftVersion)
                || neoForgeVersion.startsWith(minecraftVersion + ".")
                || neoForgeVersion.startsWith(minecraftVersion + "-");
    }

    private static final Comparator<String> VERSION_ORDER = NeoVersionFetcher::compareVersions;

    private static int compareVersions(String left, String right) {
        List<Token> a = tokenize(left);
        List<Token> b = tokenize(right);
        int max = Math.max(a.size(), b.size());

        for (int i = 0; i < max; i++) {
            if (i >= a.size()) {
                return rightHasOnlyPreReleaseTail(b, i) ? 1 : -1;
            }
            if (i >= b.size()) {
                return leftHasOnlyPreReleaseTail(a, i) ? -1 : 1;
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

    private static boolean leftHasOnlyPreReleaseTail(List<Token> tokens, int start) {
        for (int i = start; i < tokens.size(); i++) {
            if (tokens.get(i).kind == Kind.NUMBER) {
                return true;
            }
        }
        return false;
    }

    private static boolean rightHasOnlyPreReleaseTail(List<Token> tokens, int start) {
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
