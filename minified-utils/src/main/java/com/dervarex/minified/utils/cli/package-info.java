/**
 * Parsing command line arguments, with a generated help message
 * <br><br>
 * Usage:
 * <ol>
 *     <li>Create a {@link com.dervarex.minified.utils.cli.Parser}, with or without custom prefixes</li>
 *     <li>Register your arguments with {@link com.dervarex.minified.utils.cli.Parser#register}</li>
 *     <li>Give {@link com.dervarex.minified.utils.cli.Parser#parse(String[])} the args from your main method</li>
 * </ol>
 * The default prefix is "--", but you can use your own ones, like "!" and "-" <br>
 * An argument can take no value, one, two, three or any number of values with
 * {@link com.dervarex.minified.utils.cli.Parser#registerVarArgs} <br>
 * Values can't start with a prefix, var args take everything up to the next argument <br>
 * The help command is always there and prints every registered argument with its help text, same as calling the program
 * without arguments, but you can overwrite it by making your own with the name "help" <br>
 * Unknown arguments and missing values get printed to stderr, the other arguments still run
 * <br><br>
 * {@link com.dervarex.minified.utils.cli.Parser#setProgram} and {@link com.dervarex.minified.utils.cli.Parser#setDescription} change the top of the help message
 */
package com.dervarex.minified.utils.cli;
