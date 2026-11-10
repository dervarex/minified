package com.dervarex.minified.utils.exceptions;

import com.dervarex.minified.utils.json.JsonString;
import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * A controlled abort/stop signal with structured details.
 * Throw this when an operation must be terminated intentionally (user cancel,
 * precondition failed, safety stop) rather than due to an unexpected bug.
 */
@API(status = API.Status.STABLE)
public class StopException extends RuntimeException {

    /**
     * How severe the stop is
     */
    public enum Severity { INFO, WARNING, ERROR, FATAL }

    private final long timestampEpochMillis = System.currentTimeMillis();
    private final String reason; // short human-readable reason
    private final String code; // machine-readable code (e.g. "USER_CANCEL", "PRECONDITION_FAILED")
    private final Severity severity;
    private final boolean recoverable; // true if caller may retry later
    private final Map<String, String> metadata; // extra context (operation, ids, etc.)

    private StopException(Builder b) {
        super(buildMessage(b), b.cause);
        this.reason = nvl(b.reason);
        this.code = nvl(b.code);
        this.severity = b.severity;
        this.recoverable = b.recoverable;
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(b.metadata));
    }

    private static String buildMessage(Builder b) {
        StringJoiner j = new StringJoiner(" ");
        if (b.severity != null) j.add(b.severity.name());
        if (b.code != null && !b.code.isBlank()) j.add("[").add(b.code).add("]");
        if (b.reason != null) j.add(b.reason);
        return j.toString();
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    /**
     * {@return when it happened, in epoch millis}
     */
    public long getTimestampEpochMillis() { return timestampEpochMillis; }
    /**
     * {@return when it happened}
     */
    public Instant getTimestampInstant() { return Instant.ofEpochMilli(timestampEpochMillis); }
    /**
     * {@return the reason}
     */
    public String getReason() { return reason; }
    /**
     * {@return the code}
     */
    public String getCode() { return code; }
    /**
     * {@return the severity}
     */
    public Severity getSeverity() { return severity; }
    /**
     * {@return true if trying again later could work}
     */
    public boolean isRecoverable() { return recoverable; }
    /**
     * {@return the metadata}
     */
    public Map<String, String> getMetadata() { return metadata; }

    /**
     * {@return a readable summary with reason, code, severity and details}
     */
    public String toUserFriendlyMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Operation stopped: ").append(reason).append('\n');
        if (!code.isBlank()) sb.append("Code: ").append(code).append('\n');
        sb.append("Severity: ").append(severity).append(", Retryable: ").append(recoverable).append('\n');
        sb.append("Time: ").append(getTimestampInstant()).append('\n');
        if (!metadata.isEmpty()) {
            sb.append("Details:\n");
            metadata.forEach((k,v) -> sb.append("  - ").append(k).append(": ").append(v).append('\n'));
        }
        return sb.toString();
    }

    /**
     * {@return everything as JSON}
     */
    public String toJson() {
        StringBuilder meta = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String,String> e : metadata.entrySet()) {
            if (!first) meta.append(',');
            first = false;
            meta.append(escape(e.getKey())).append(':').append(escape(e.getValue()));
        }
        meta.append('}');
        return "{" +
                "\"timestamp\":" + timestampEpochMillis + "," +
                "\"reason\":" + escape(reason) + "," +
                "\"code\":" + escape(code) + "," +
                "\"severity\":" + escape(severity == null ? "" : severity.name()) + "," +
                "\"recoverable\":" + recoverable + "," +
                "\"metadata\":" + meta +
                "}";
    }

    private static String escape(String s) {
        return s == null ? "null" : new JsonString(s).toJson();
    }

    // Convenience factories
    /**
     * The user cancelled, recoverable
     *
     * @param reason why
     * @return a new {@link StopException}
     */
    public static StopException userCancel(String reason) {
        return new Builder().severity(Severity.INFO).code("USER_CANCEL").reason(reason).recoverable(true).build();
    }
    /**
     * Something needed wasn't there, recoverable
     *
     * @param reason why
     * @return a new {@link StopException}
     */
    public static StopException preconditionFailed(String reason) {
        return new Builder().severity(Severity.WARNING).code("PRECONDITION_FAILED").reason(reason).recoverable(true).build();
    }
    /**
     * A stop that can't be recovered
     *
     * @param reason why
     * @return a new {@link StopException}
     */
    public static StopException fatal(String reason) {
        return new Builder().severity(Severity.FATAL).code("FATAL_STOP").reason(reason).recoverable(false).build();
    }

    /**
     * Builds a {@link StopException}
     */
    public static class Builder {
        /**
         * Creates the builder
         */
        public Builder() {
        }

        private String reason = "";
        private String code = "";
        private Severity severity = Severity.ERROR;
        private boolean recoverable = false;
        private final Map<String, String> metadata = new LinkedHashMap<>();
        private Throwable cause;

        /**
         * Sets the reason
         *
         * @param r the reason
         * @return this builder
         */
        public Builder reason(String r) { this.reason = r; return this; }
        /**
         * Sets the code
         *
         * @param c the code
         * @return this builder
         */
        public Builder code(String c) { this.code = c; return this; }
        /**
         * Sets the severity
         *
         * @param s the severity
         * @return this builder
         */
        public Builder severity(Severity s) { this.severity = s; return this; }
        /**
         * Sets if trying again later could work
         *
         * @param r true if it could
         * @return this builder
         */
        public Builder recoverable(boolean r) { this.recoverable = r; return this; }
        /**
         * Adds a detail
         *
         * @param k the name
         * @param v the value
         * @return this builder
         */
        public Builder meta(String k, String v) { this.metadata.put(k, v); return this; }
        /**
         * Adds details
         *
         * @param m the details, null is ignored
         * @return this builder
         */
        public Builder metadata(Map<String,String> m) { if (m!=null) m.forEach(this::meta); return this; }
        /**
         * Sets the cause
         *
         * @param t the cause
         * @return this builder
         */
        public Builder cause(Throwable t) { this.cause = t; return this; }
        /**
         * {@return a new {@link StopException}}
         */
        public StopException build() { return new StopException(this); }
    }
}
