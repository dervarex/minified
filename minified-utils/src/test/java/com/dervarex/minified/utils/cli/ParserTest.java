package com.dervarex.minified.utils.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
    Parser parser;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setup() {
        // Capture System.out and System.err
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        parser = new Parser("--", "-", "!");
        parser.register("hello", () -> {
            System.out.println("Hello World!");
        });
        parser.register("onearg", (String arg) -> {
            System.out.println("Given Argument: " + arg);
        });

        parser.setDescription("Test for minified's CLI Parsing Utility");

        parser.addPrefix("1");

        parser.register("add", "Add two numbers together", (String a, String b) -> {
            int result = Integer.parseInt(a) + Integer.parseInt(b);
            System.out.println(a + " + " + b + " = " + result);
        });
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        outContent.reset();
        errContent.reset();
    }


    @Test
    public void testSimpleCommandWithDifferentPrefixes() {
        parser.parse(new String[]{"-hello"});
        assertEquals("Hello World!\n", outContent.toString(), "Should work with - prefix");

        outContent.reset();
        parser.parse(new String[]{"!hello"});
        assertEquals("Hello World!\n", outContent.toString(), "Should work with ! prefix");

        outContent.reset();
        parser.parse(new String[]{"1hello"});
        assertEquals("Hello World!\n", outContent.toString(), "Should work with 1 prefix");
    }

    @Test
    public void testOneArgCommand() {
        parser.parse(new String[]{"--onearg", "test123"});

        assertEquals("Given Argument: test123\n", outContent.toString(), "Should print the given argument");
    }

    @Test
    public void testOneArgCommandMissingArgument() {
        parser.parse(new String[]{"--onearg"});

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Missing argument"), "Should show missing argument error");
        assertTrue(errOutput.contains("--onearg"), "Should show which command is missing argument");
    }

    @Test
    public void testOneArgCommandWithFlagAsArgument() {
        parser.parse(new String[]{"--onearg", "--hello"});

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Missing argument"), "Should not treat flag as argument");
    }

    @Test
    public void testTwoArgCommand() {
        parser.parse(new String[]{"--add", "5", "3"});

        assertEquals("5 + 3 = 8\n", outContent.toString(), "Should add two numbers");
    }

    @Test
    public void testTwoArgCommandMissingOneArgument() {
        parser.parse(new String[]{"--add", "5"});

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Missing arguments"), "Should show missing arguments error");
    }

    @Test
    public void testTwoArgCommandMissingAllArguments() {
        parser.parse(new String[]{"--add"});

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Missing arguments"), "Should show missing arguments error");
    }

    @Test
    public void testUnknownCommand() {
        parser.parse(new String[]{"--nonexistent"});

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Unknown argument"), "Should show unknown argument error");
        assertTrue(errOutput.contains("--nonexistent"), "Should show the unknown command");
    }

    @Test
    public void testMultipleCommands() {
        parser.parse(new String[]{"--hello", "--onearg", "world", "--add", "10", "5"});

        String output = outContent.toString();
        assertTrue(output.contains("Hello World!"), "Should execute hello");
        assertTrue(output.contains("Given Argument: world"), "Should execute onearg");
        assertTrue(output.contains("10 + 5 = 15"), "Should execute add");

        // Check order
        String[] lines = output.split("\n");
        assertEquals(3, lines.length, "Should have 3 lines of output");
        assertEquals("Hello World!", lines[0], "Hello should be first");
        assertEquals("Given Argument: world", lines[1], "onearg should be second");
        assertEquals("10 + 5 = 15", lines[2], "add should be third");
    }

    @Test
    public void testMixedPrefixes() {
        parser.parse(new String[]{"-hello", "!onearg", "test", "1add", "7", "2"});

        String output = outContent.toString();
        assertTrue(output.contains("Hello World!"), "Should execute -hello");
        assertTrue(output.contains("Given Argument: test"), "Should execute !onearg");
        assertTrue(output.contains("7 + 2 = 9"), "Should execute 1add");
    }

    @Test
    public void testInvalidNumberFormat() {
        parser.parse(new String[]{"--add", "abc", "def"});

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("NumberFormatException") ||
                        errOutput.contains("For input string"),
                "Should show number format exception");
    }

    @Test
    public void testRegisterReturnsParser() {
        Parser returnedParser = parser.register("test", () -> {
            System.out.println("test");
        });

        assertSame(parser, returnedParser, "Register should return the same parser for chaining");
    }

    @Test
    public void testChainedRegistration() {
        parser.register("test1", () -> System.out.println("Test 1"))
                .register("test2", () -> System.out.println("Test 2"))
                .register("test3", () -> System.out.println("Test 3"));

        parser.parse(new String[]{"--test1", "--test2", "--test3"});

        String output = outContent.toString();
        assertTrue(output.contains("Test 1"), "Should execute test1");
        assertTrue(output.contains("Test 2"), "Should execute test2");
        assertTrue(output.contains("Test 3"), "Should execute test3");
    }

    @Test
    public void testOverwriteCommand() {
        parser.register("hello", () -> {
            System.out.println("Overwritten Hello!");
        });

        parser.parse(new String[]{"--hello"});

        assertEquals("Overwritten Hello!\n", outContent.toString(), "Should use the new handler");
    }

    @Test
    public void testCommandWithSpacesInArgument() {
        parser.parse(new String[]{"--onearg", "Hello World"});

        assertEquals("Given Argument: Hello World\n", outContent.toString(),
                "Should handle spaces in arguments");
    }
}