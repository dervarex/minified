package com.dervarex.minified.utils.exceptions;

import com.dervarex.minified.utils.json.JsonString;
import org.apiguardian.api.API;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extended Exception for missing network connection.
 * <p>
 * Gives Diagnostics (DNS, TCP, HTTP Tests), user suggestions and can be serialized as JSON.
 */
@API(status = API.Status.STABLE)
public class NoConnectionException extends Exception {

    private final long timestampEpochMillis = System.currentTimeMillis();
    private final String actionContext;
    private final Boolean dnsResolved;
    private final boolean tcpAnyReachable;
    private final boolean httpAnyReachable;
    private final Map<String, ProbeResult> probeResults;
    private final List<String> suggestions;
    private final String activeOs;
    private final String networkInterfaceSummary;
    private final Throwable rootCause;
    private transient String cachedMessage;

    private NoConnectionException(Builder b) {
        super(makeBaseMessage(b), b.rootCause);
        this.actionContext = b.actionContext;
        this.dnsResolved = b.dnsResolved;
        this.tcpAnyReachable = b.tcpAnyReachable;
        this.httpAnyReachable = b.httpAnyReachable;
        this.probeResults = Collections.unmodifiableMap(new LinkedHashMap<>(b.probeResults));
        this.suggestions = Collections.unmodifiableList(new ArrayList<>(b.suggestions));
        this.activeOs = b.activeOs;
        this.networkInterfaceSummary = b.networkInterfaceSummary;
        this.rootCause = b.rootCause;
    }

    private static String makeBaseMessage(Builder b) {
        return "No internet connection (" + b.actionContext + ") - DNS=" + b.dnsResolved + ", TCP=" + b.tcpAnyReachable + ", HTTP=" + b.httpAnyReachable;
    }

    /**
     * {@return the action context}
     */
    public String getActionContext() { return actionContext; }
    /**
     * {@return true if DNS worked}
     */
    public boolean wasDnsResolved() { return dnsResolved; }
    /**
     * {@return true if any host was reachable over TCP}
     */
    public boolean wasAnyTcpReachable() { return tcpAnyReachable; }
    /**
     * {@return true if any host answered over HTTP}
     */
    public boolean wasAnyHttpReachable() { return httpAnyReachable; }
    /**
     * {@return when it happened, in epoch millis}
     */
    public long getTimestampEpochMillis() { return timestampEpochMillis; }
    /**
     * {@return when it happened}
     */
    public Instant getTimestampInstant() { return Instant.ofEpochMilli(timestampEpochMillis); }
    /**
     * {@return the probe results}
     */
    public Map<String, ProbeResult> getProbeResults() { return probeResults; }
    /**
     * {@return the suggestions}
     */
    public List<String> getSuggestions() { return suggestions; }
    /**
     * {@return the os}
     */
    public String getActiveOs() { return activeOs; }
    /**
     * {@return the network interface summary}
     */
    public String getNetworkInterfaceSummary() { return networkInterfaceSummary; }
    /**
     * {@return the root cause}
     */
    public Throwable getRootCause() { return rootCause; }

    /**
     * {@return a readable summary with the checks and suggestions}
     */
    public String toUserFriendlyMessage() {
        if (cachedMessage != null) return cachedMessage;
        StringBuilder sb = new StringBuilder();
        sb.append("Network Problem for Action: ").append(actionContext).append('\n');
        sb.append("OS: ").append(activeOs).append(" - Time: ").append(getTimestampInstant()).append('\n');
        sb.append("DNS ok: ").append(dnsResolved).append(", TCP reachable: ").append(tcpAnyReachable)
          .append(", HTTP reachable: ").append(httpAnyReachable).append('\n');
        sb.append("Probes:\n");
        probeResults.forEach((k,v) -> sb.append("  - ").append(k).append(": ").append(v).append('\n'));
        if (!suggestions.isEmpty()) {
            sb.append("Suggestions:\n");
            suggestions.forEach(s -> sb.append("  * ").append(s).append('\n'));
        }
        cachedMessage = sb.toString();
        return cachedMessage;
    }

    /**
     * {@return everything as JSON}
     */
    public String toJson() {
        String probesJson = probeResults.entrySet().stream()
                .map(e -> escape(e.getKey()) + ":" + e.getValue().toJson())
                .collect(Collectors.joining(","));
        String suggJson = suggestions.stream().map(NoConnectionException::escape).collect(Collectors.joining(","));
        return "{" +
                "\"action\":" + escape(actionContext) + "," +
                "\"timestamp\":" + timestampEpochMillis + "," +
                "\"os\":" + escape(activeOs) + "," +
                "\"dnsResolved\":" + dnsResolved + "," +
                "\"tcpAnyReachable\":" + tcpAnyReachable + "," +
                "\"httpAnyReachable\":" + httpAnyReachable + "," +
                "\"probes\":{" + probesJson + "}," +
                "\"suggestions\":[" + suggJson + "]," +
                "\"networkIface\":" + escape(networkInterfaceSummary) +
                "}";
    }

    private static String escape(String s) {
        return s == null ? "null" : new JsonString(s).toJson();
    }

    /**
     * {@return same as {@link #toUserFriendlyMessage()}}
     */
    @Override
    public String toString() { return toUserFriendlyMessage(); }

    /**
     * The result of one connection check
     */
    public static final class ProbeResult {
        /**
         * What got checked
         */
        public enum Type { DNS, TCP, HTTP }
        private final Type type;
        private final boolean success;
        private final long latencyMillis;
        private final String target;
        private final String detail;
        /**
         * @param type what got checked
         * @param success true if it worked
         * @param latencyMillis how long it took, -1 if unknown
         * @param target the host or url
         * @param detail what happened
         */
        public ProbeResult(Type type, boolean success, long latencyMillis, String target, String detail) {
            this.type = type; this.success = success; this.latencyMillis = latencyMillis; this.target = target; this.detail = detail;
        }
        /**
         * {@return the result as JSON}
         */
        public String toJson() {
            return "{" +
                    "\"type\":" + escape(type.name()) + "," +
                    "\"success\":" + success + "," +
                    "\"latency\":" + latencyMillis + "," +
                    "\"target\":" + escape(target) + "," +
                    "\"detail\":" + escape(detail) +
                    "}";
        }
        /**
         * {@return the result in one line}
         */
        @Override public String toString() { return type+"("+target+") success="+success+" latency="+latencyMillis+"ms detail="+detail; }
    }

    /**
     * Builds a {@link NoConnectionException}
     */
    public static class Builder {
        /**
         * Creates the builder
         */
        public Builder() {
        }

        private String actionContext = "unknown";
        private boolean dnsResolved;
        private boolean tcpAnyReachable;
        private boolean httpAnyReachable;
        private final Map<String, ProbeResult> probeResults = new LinkedHashMap<>();
        private final List<String> suggestions = new ArrayList<>();
        private String activeOs = System.getProperty("os.name");
        private String networkInterfaceSummary = "";
        private Throwable rootCause;
        /**
         * Sets the action context
         *
         * @param ctx the action context
         * @return this builder
         */
        public Builder action(String ctx) { this.actionContext = ctx; return this; }
        /**
         * Sets if DNS worked
         *
         * @param v true if it worked
         * @return this builder
         */
        public Builder dnsResolved(boolean v) { this.dnsResolved = v; return this; }
        /**
         * Sets if any host was reachable over TCP
         *
         * @param v true if one was
         * @return this builder
         */
        public Builder tcpAny(boolean v) { this.tcpAnyReachable = v; return this; }
        /**
         * Sets if any host answered over HTTP
         *
         * @param v true if one did
         * @return this builder
         */
        public Builder httpAny(boolean v) { this.httpAnyReachable = v; return this; }
        /**
         * Adds a check result
         *
         * @param key the name of the check
         * @param pr the result
         * @return this builder
         */
        public Builder addProbe(String key, ProbeResult pr) { this.probeResults.put(key, pr); return this; }
        /**
         * Adds a suggestion for the user
         *
         * @param s the suggestion
         * @return this builder
         */
        public Builder addSuggestion(String s) { if (s!=null && !s.isBlank()) suggestions.add(s); return this; }
        /**
         * Adds suggestions for the user
         *
         * @param s the suggestions
         * @return this builder
         */
        public Builder suggestions(Collection<String> s) { s.forEach(this::addSuggestion); return this; }
        /**
         * Sets the os
         *
         * @param os the os
         * @return this builder
         */
        public Builder os(String os) { this.activeOs = os; return this; }
        /**
         * Sets the network interface summary
         *
         * @param summary the network interface summary
         * @return this builder
         */
        public Builder netIfaces(String summary) { this.networkInterfaceSummary = summary; return this; }
        /**
         * Sets the root cause
         *
         * @param t the root cause
         * @return this builder
         */
        public Builder cause(Throwable t) { this.rootCause = t; return this; }
        /**
         * Without any suggestions, we add some based on the checks
         *
         * @return a new {@link NoConnectionException}
         */
        public NoConnectionException build() { if (suggestions.isEmpty()) autoPopulateSuggestions(); return new NoConnectionException(this); }
        private void autoPopulateSuggestions() {
            if (!dnsResolved) suggestions.add("Check DNS and Router");
            if (!tcpAnyReachable) suggestions.add("Check your firewall (and vpn if present)");
            if (!httpAnyReachable && tcpAnyReachable) suggestions.add("You might be on a captive portal(hotels often have that)");
            suggestions.add("Disconnect from the wifi and reconnect");
            suggestions.add("Try again later, the problem might be on the server side");
        }
    }

    /**
     * Runs a quick check and throws if it fails
     *
     * @param action what needed the network
     * @param quickCheck returns true if online, an exception counts as offline
     * @throws NoConnectionException if the check fails
     */
    public static void throwIfOffline(String action, java.util.function.Supplier<Boolean> quickCheck) throws NoConnectionException {
        boolean ok;
        try { ok = quickCheck.get(); } catch (Throwable t) { ok = false; }
        if (!ok) {
            throw new Builder()
                    .action(action)
                    .dnsResolved(false)
                    .tcpAny(false)
                    .httpAny(false)
                    .addProbe("quick", new ProbeResult(ProbeResult.Type.TCP, false, -1, "quickCheck", "returned false"))
                    .build();
        }
    }
}
