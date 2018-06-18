package life.qbic.cli;

import picocli.CommandLine.Option;

/**
 * Abstract class that encapsulates all common command-line arguments (i.e., a <i>command</i>).
 *
 * Because we want to avoid using verbose getter/setter methods for a data structure that only hold incoming command-line arguments, we cannot use interfaces,
 * so the second-best approach is to use an abstract class from which all commands should derive.
 *
 * Command-classes are used as a simple way to bridge argument parsing from concrete implementations.
 */
public abstract class AbstractCommand {

    /**
     * Standard <code>-v/--version</code> to print version and exit.
     */
    @Option(names = {"-v", "--version"}, description = "Prints version and exits.", versionHelp = true)
    public volatile boolean printVersion;
    /**
     * Standard <code>-h/--help</code> to print help and exit.
     */
    @Option(names = {"-h", "--help"}, description = "Prints usage and exists.", usageHelp = true)
    public volatile boolean printHelp;
}
