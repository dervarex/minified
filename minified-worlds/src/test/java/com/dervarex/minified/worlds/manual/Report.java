package com.dervarex.minified.worlds.manual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

final class Report {

    enum Status { PASS, WARN, FAIL, SKIP }

    record Result(String section, String name, Status status, String detail) {}

    @FunctionalInterface
    interface Check {
        String run() throws Exception;
    }

    private static final int LIST_LIMIT = 15;
    private static final int OVERVIEW_DETAIL_LIMIT = 160;

    private final List<Result> results = new ArrayList<>();
    private final Map<String, String> facts = new LinkedHashMap<>();
    private final long startedAt = System.nanoTime();
    private String section = "General";

    static RuntimeException warning(String message) {
        return new Warning(message);
    }

    static RuntimeException skipped(String reason) {
        return new Skipped(reason);
    }

    void section(String name) {
        section = name;
        System.out.println();
        System.out.println("[" + name + "]");
    }

    void log(String line) {
        System.out.println("    " + line);
    }

    void list(Collection<?> items) {
        items.stream().limit(LIST_LIMIT).forEach(item -> log("- " + item));
        if (items.size() > LIST_LIMIT) {
            log("... (" + (items.size() - LIST_LIMIT) + " more)");
        }
    }

    void fact(String key, Object value) {
        facts.put(key, String.valueOf(value));
    }

    void skip(String name, String reason) {
        record(name, Status.SKIP, reason, 0);
    }

    void check(String name, Check check) {
        long start = System.nanoTime();
        Status status;
        String detail;
        try {
            detail = check.run();
            status = Status.PASS;
        } catch (Warning warning) {
            detail = warning.getMessage();
            status = Status.WARN;
        } catch (Skipped skipped) {
            detail = skipped.getMessage();
            status = Status.SKIP;
        } catch (AssertionError e) {
            detail = e.getMessage();
            status = Status.FAIL;
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            detail = t.getClass().getSimpleName() + ": " + t.getMessage();
            status = Status.FAIL;
        }
        record(name, status, detail, (System.nanoTime() - start) / 1_000_000);
    }

    <T> T load(String name, Callable<T> loader) {
        AtomicReference<T> loaded = new AtomicReference<>();
        check(name, () -> {
            loaded.set(loader.call());
            return "";
        });
        return loaded.get();
    }

    long count(Status status) {
        return results.stream().filter(result -> result.status() == status).count();
    }

    void printOverview() {
        System.out.println();
        System.out.println();
        System.out.println("Overview");
        System.out.println();

        int keyWidth = facts.keySet().stream().mapToInt(String::length).max().orElse(0);
        facts.forEach((key, value) -> System.out.printf("  %-" + keyWidth + "s  %s%n", key, value));

        System.out.println();
        String row = "  %-" + results.stream().mapToInt(result -> result.section().length()).max().orElse(0) + "s %5s %5s %5s %5s%n";
        System.out.printf(row, "", "pass", "warn", "fail", "skip");
        results.stream().map(Result::section).distinct().forEach(name -> {
            List<Result> inSection = results.stream().filter(result -> result.section().equals(name)).toList();
            System.out.printf(row, name,
                    countOrDot(inSection, Status.PASS), countOrDot(inSection, Status.WARN),
                    countOrDot(inSection, Status.FAIL), countOrDot(inSection, Status.SKIP));
        });

        printResults("Skipped", Status.SKIP);
        printResults("Warnings", Status.WARN);
        printResults("Failures", Status.FAIL);

        System.out.println();
        System.out.printf("  %d passed, %d warnings, %d failed, %d skipped in %.1fs%n",
                count(Status.PASS), count(Status.WARN), count(Status.FAIL), count(Status.SKIP),
                (System.nanoTime() - startedAt) / 1e9);
    }

    private void record(String name, Status status, String detail, long millis) {
        results.add(new Result(section, name, status, detail));
        String suffix = detail == null || detail.isBlank() ? "" : " - " + detail;
        String timing = status == Status.SKIP ? "" : " (" + millis + " ms)";
        System.out.println("  " + status + "  " + name + suffix + timing);
    }

    private void printResults(String title, Status status) {
        List<Result> matching = results.stream().filter(result -> result.status() == status).toList();
        if (matching.isEmpty()) return;

        System.out.println();
        System.out.println("  " + title);
        matching.forEach(result -> System.out.println("    " + result.section() + " > " + result.name()
                + (result.detail() == null || result.detail().isBlank() ? "" : ": " + shorten(result.detail()))));
    }

    private static String countOrDot(List<Result> results, Status status) {
        long count = results.stream().filter(result -> result.status() == status).count();
        return count == 0 ? "." : String.valueOf(count);
    }

    private static String shorten(String detail) {
        String oneLine = detail.replace('\n', ' ');
        return oneLine.length() <= OVERVIEW_DETAIL_LIMIT ? oneLine : oneLine.substring(0, OVERVIEW_DETAIL_LIMIT) + "...";
    }

    private static final class Warning extends RuntimeException {
        Warning(String message) {
            super(message);
        }
    }

    private static final class Skipped extends RuntimeException {
        Skipped(String reason) {
            super(reason);
        }
    }
}
