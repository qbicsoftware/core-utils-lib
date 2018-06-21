package life.qbic.cli;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import life.qbic.exceptions.ApplicationException;
import org.apache.commons.lang3.Validate;
import picocli.CommandLine;
import picocli.CommandLine.Option;

/**
 * Abstract class that encapsulates all common command-line arguments using {@code picocli} annotations.
 *
 * Because we want to avoid using verbose getter/setter methods for a data structure that only hold incoming command-line arguments, we cannot use interfaces.
 *
 * The second-best approach is to use an abstract class from which all commands should derive. Command-classes are used as a simple way to bridge argument
 * parsing from concrete implementations.
 *
 * All classes extending this class must contain a public constructor that takes no arguments.
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

    /**
     * Utility method to parse the given command-line arguments as a command using the {@link CommandLine#populateCommand(Object, String...)} method.
     *
     * @param commandClass the class of the desired command.
     * @param args the command-line arguments
     * @param <T> the type of the desired commands.
     * @return an instance of {@code T} containing the parsed arguments as class members.
     */
    public static <T extends AbstractCommand> T parseArguments(final Class<T> commandClass, final String[] args) {
        Validate.notNull(commandClass, "commandClass is required and cannot be null");
        Validate.notNull(args, "args is required and cannot be null");

        final Constructor<T> constructor;
        try {
            constructor = commandClass.getConstructor();
        } catch (final NoSuchMethodException e) {
            throw new ApplicationException(String
                .format("Could not find a no-arguments public constructor for the given command with class name %s.", commandClass),
                e);
        }

        final T dummyCommand;
        try {
            dummyCommand = constructor.newInstance();
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ApplicationException(String
                .format("Could not create a new instance of the command (class name: %s). The class does not contain a no-arg constructor.", commandClass), e);
        }

        return CommandLine.populateCommand(dummyCommand, args);
    }
}
