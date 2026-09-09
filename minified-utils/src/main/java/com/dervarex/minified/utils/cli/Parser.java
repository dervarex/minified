package com.dervarex.minified.utils.cli;

import java.util.*;
import java.util.function.Consumer;

public class Parser {

    private final Map<String, CommandHandler> commands = new LinkedHashMap<>();
    private final Set<String> prefixes = new LinkedHashSet<>();
    private String description = "";

    @FunctionalInterface
    public interface NoArgCommand {
        void execute();
    }

    @FunctionalInterface
    public interface OneArgCommand<T> {
        void execute(T arg);
    }

    @FunctionalInterface
    public interface TwoArgCommand<T, U> {
        void execute(T arg1, U arg2);
    }

    @FunctionalInterface
    public interface ThreeArgCommand<T, U, V> {
        void execute(T arg1, U arg2, V arg3);
    }

    @FunctionalInterface
    public interface VarArgsCommand {
        void execute(String... args);
    }

    private static class CommandHandler {
        final Object handler;
        final int paramCount;
        final String flag;
        final String helpText;

        CommandHandler(String flag, Object handler, int paramCount, String helpText) {
            this.flag = flag;
            this.handler = handler;
            this.paramCount = paramCount;
            this.helpText = helpText;
        }
    }

    // Default Prefix
    public Parser() {
        this.prefixes.add("--");
    }

    // Constructor with custom prefixes
    public Parser(String... prefixes) {
        if (prefixes.length == 0) {
            this.prefixes.add("--");
        } else {
            this.prefixes.addAll(Arrays.asList(prefixes));
        }
    }

    // change prefixes while running
    public Parser setPrefixes(String... prefixes) {
        this.prefixes.clear();
        this.prefixes.addAll(Arrays.asList(prefixes));
        return this;
    }

    public Parser addPrefix(String prefix) {
        this.prefixes.add(prefix);
        return this;
    }

    public Parser setDescription(String description) {
        this.description = description;
        return this;
    }

    // register methods with help text
    public Parser register(String flag, String helpText, NoArgCommand handler) {
        commands.put(normalizeFlag(flag), new CommandHandler(flag, handler, 0, helpText));
        return this;
    }

    public Parser register(String flag, String helpText, Consumer<String> handler) {
        commands.put(normalizeFlag(flag), new CommandHandler(flag, handler, 1, helpText));
        return this;
    }

    public Parser register(String flag, String helpText, TwoArgCommand<String, String> handler) {
        commands.put(normalizeFlag(flag), new CommandHandler(flag, handler, 2, helpText));
        return this;
    }

    public Parser register(String flag, String helpText, ThreeArgCommand<String, String, String> handler) {
        commands.put(normalizeFlag(flag), new CommandHandler(flag, handler, 3, helpText));
        return this;
    }

    public Parser registerVarArgs(String flag, String helpText, VarArgsCommand handler) {
        commands.put(normalizeFlag(flag), new CommandHandler(flag, handler, -1, helpText));
        return this;
    }

    // without help text
    public Parser register(String flag, NoArgCommand handler) {
        return register(flag, "", handler);
    }

    public Parser register(String flag, Consumer<String> handler) {
        return register(flag, "", handler);
    }

    public Parser register(String flag, TwoArgCommand<String, String> handler) {
        return register(flag, "", handler);
    }

    public Parser register(String flag, ThreeArgCommand<String, String, String> handler) {
        return register(flag, "", handler);
    }

    public Parser registerVarArgs(String flag, VarArgsCommand handler) {
        return registerVarArgs(flag, "", handler);
    }

    // normalize flags (remove prefixes)
    private String normalizeFlag(String flag) {
        for (String prefix : prefixes) {
            if (flag.startsWith(prefix)) {
                return flag.substring(prefix.length());
            }
        }
        return flag;
    }

    // check and extract flag names
    private String extractFlag(String arg) {
        for (String prefix : prefixes) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return null;
    }

    // Generate help message
    private void showHelp() {
        System.out.println("Usage: program [options]");
        if (!description.isEmpty()) {
            System.out.println(description);
        }
        System.out.println();
        System.out.println("Options:");

        // help argument itself
        System.out.printf("  %-20s %s%n", formatFlag("help"), "Show this help message");

        for (CommandHandler cmd : commands.values()) {
            String flagDisplay = formatFlag(cmd.flag);
            String helpText = cmd.helpText.isEmpty() ? "No description" : cmd.helpText;
            System.out.printf("  %-20s %s%n", flagDisplay, helpText);
        }
    }

    private String formatFlag(String flag) {
        StringBuilder sb = new StringBuilder();
        for (String prefix : prefixes) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(prefix).append(flag);
        }
        return sb.toString();
    }

    public void parse(String[] args) {
        if (args.length == 0) {
            showHelp();
            return;
        }

        // check for help flag
        for (String arg : args) {
            String flagName = extractFlag(arg);
            if (flagName != null && flagName.equals("help")) {
                showHelp();
                return;
            }
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            String flagName = extractFlag(arg);

            if (flagName == null) {
                System.err.println("Unknown argument (missing prefix?): " + arg);
                continue;
            }

            CommandHandler command = commands.get(flagName);

            if (command == null) {
                System.err.println("Unknown argument: " + arg);
                continue;
            }

            switch (command.paramCount) {
                case 0:
                    ((NoArgCommand) command.handler).execute();
                    break;

                case 1:
                    if (i + 1 < args.length && extractFlag(args[i + 1]) == null) {
                        ((Consumer<String>) command.handler).accept(args[++i]);
                    } else {
                        System.err.println("Missing argument for " + arg);
                        System.err.println("Expected: " + formatFlag(command.flag) + " <value>");
                    }
                    break;

                case 2:
                    if (i + 2 < args.length &&
                            extractFlag(args[i + 1]) == null &&
                            extractFlag(args[i + 2]) == null) {
                        String arg1 = args[++i];
                        String arg2 = args[++i];
                        ((TwoArgCommand<String, String>) command.handler).execute(arg1, arg2);
                    } else {
                        System.err.println("Missing arguments for " + arg);
                        System.err.println("Expected: " + formatFlag(command.flag) + " <value1> <value2>");
                    }
                    break;

                case 3:
                    if (i + 3 < args.length &&
                            extractFlag(args[i + 1]) == null &&
                            extractFlag(args[i + 2]) == null &&
                            extractFlag(args[i + 3]) == null) {
                        String arg1 = args[++i];
                        String arg2 = args[++i];
                        String arg3 = args[++i];
                        ((ThreeArgCommand<String, String, String>) command.handler).execute(arg1, arg2, arg3);
                    } else {
                        System.err.println("Missing arguments for " + arg);
                        System.err.println("Expected: " + formatFlag(command.flag) + " <value1> <value2> <value3>");
                    }
                    break;

                case -1: // VarArgs
                    List<String> cmdArgs = new ArrayList<>();
                    while (i + 1 < args.length && extractFlag(args[i + 1]) == null) {
                        cmdArgs.add(args[++i]);
                    }
                    if (cmdArgs.isEmpty()) {
                        System.err.println("Missing arguments for " + arg);
                    } else {
                        ((VarArgsCommand) command.handler).execute(cmdArgs.toArray(new String[0]));
                    }
                    break;
            }
        }
    }
}