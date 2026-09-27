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
        parser.register("hello", "Print hello world", () -> {
            System.out.println("Hello World!");
        });
        parser.register("onearg", "Print a single argument", (String arg) -> {
            System.out.println("Given Argument: " + arg);
        });

        parser.setDescription("Test for minified's CLI Parsing Utility");

        parser.addPrefix("1"); // I absolutely do not recommend you to use 1 as prefix, since this breaks any numbers starting with 1, but we're testing it anyway

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
        parser.parse(new String[]{"--hello", "--onearg", "world", "--add", "20", "5"});

        String output = outContent.toString();
        System.out.println("Actual output: " + output); // Debug output

        assertTrue(output.contains("Hello World!"), "Should execute hello");
        assertTrue(output.contains("Given Argument: world"), "Should execute onearg");
        assertTrue(output.contains("20 + 5 = 25"), "Should execute add");

        // Check order
        String[] lines = output.split("\n");
        assertEquals(3, lines.length, "Should have 3 lines of output");
        assertEquals("Hello World!", lines[0], "Hello should be first");
        assertEquals("Given Argument: world", lines[1], "onearg should be second");
        assertEquals("20 + 5 = 25", lines[2], "add should be third");
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
        assertThrows(NumberFormatException.class, () -> {
            parser.parse(new String[]{"--add", "abc", "def"});
        }, "Should throw NumberFormatException for invalid numbers");
    }

    @Test
    public void testRegisterReturnsParser() {
        Parser returnedParser = parser.register("test", "Test command", () -> {
            System.out.println("test");
        });

        assertSame(parser, returnedParser, "Register should return the same parser for chaining");
    }

    @Test
    public void testChainedRegistration() {
        parser.register("test1", "Test 1", () -> System.out.println("Test 1"))
                .register("test2", "Test 2", () -> System.out.println("Test 2"))
                .register("test3", "Test 3", () -> System.out.println("Test 3"));

        parser.parse(new String[]{"--test1", "--test2", "--test3"});

        String output = outContent.toString();
        assertTrue(output.contains("Test 1"), "Should execute test1");
        assertTrue(output.contains("Test 2"), "Should execute test2");
        assertTrue(output.contains("Test 3"), "Should execute test3");
    }

    @Test
    public void testOverwriteCommand() {
        parser.register("hello", "Overwritten hello", () -> {
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

    @Test
    public void testHelpCommand() {
        parser.parse(new String[]{"--help"});

        String output = outContent.toString();
        assertTrue(output.contains("Usage:"), "Should show usage information");
        assertTrue(output.contains("Test for minified's CLI Parsing Utility"), "Should show description");
        assertTrue(output.contains("hello"), "Should list hello command");
        assertTrue(output.contains("onearg"), "Should list onearg command");
        assertTrue(output.contains("add"), "Should list add command");
        assertTrue(output.contains("help"), "Should list help command");
    }

    @Test
    public void testEmptyArgs() {
        parser.parse(new String[]{});

        String output = outContent.toString();
        assertTrue(output.contains("Usage:"), "Should show help when no arguments given");
    }

    @Test
    public void testSetProgramName() {
        parser.setProgram("myprogram");
        parser.parse(new String[]{"--help"});

        String output = outContent.toString();
        assertTrue(output.contains("Usage: myprogram"), "Should show custom program name");
    }

    @Test
    public void testNoPrefixArgument() {
        parser.parse(new String[]{"hello"});

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Unknown argument"), "Should show error for argument without prefix");
        assertTrue(errOutput.contains("missing prefix"), "Should mention missing prefix");
    }

    @Test
    public void testVarArgsCommand() {
        parser.registerVarArgs("echo", "Echo all arguments", args -> {
            System.out.println(String.join(" ", args));
        });

        parser.parse(new String[]{"--echo", "Hello", "World", "from", "CLI"});

        assertEquals("Hello World from CLI\n", outContent.toString(), "Should echo all arguments");
    }

    @Test
    public void testVarArgsCommandMissingArguments() {
        parser.registerVarArgs("echo", "Echo all arguments", args -> {
            System.out.println(String.join(" ", args));
        });

        parser.parse(new String[]{"--echo"});

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Missing arguments"), "Should show error for missing varargs");
    }

    @Test
    public void testThreeArgCommand() {
        parser.register("concat", "Concatenate three strings", (String a, String b, String c) -> {
            System.out.println(a + b + c);
        });

        parser.parse(new String[]{"--concat", "foo", "bar", "baz"});

        assertEquals("foobarbaz\n", outContent.toString(), "Should concatenate three arguments");
    }

    @Test
    public void testThreeArgCommandMissingArguments() {
        parser.register("concat", "Concatenate three strings", (String a, String b, String c) -> {
            System.out.println(a + b + c);
        });

        parser.parse(new String[]{"--concat", "foo"});

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Missing arguments"), "Should show error for missing arguments");
    }
}