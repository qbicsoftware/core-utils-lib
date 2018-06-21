package life.qbic.javafx;

import javafx.application.Application;
import life.qbic.cli.AbstractCommand;

/**
 * Base class for stand-alone JavaFX applications.
 */
public abstract class QBiCApplication<T extends AbstractCommand> extends Application {

    /**
     * @return the parsed command-line arguments.
     */
    protected final T getCommand(final Class<T> commandClass) {
        return AbstractCommand.parseArguments(commandClass, super.getParameters().getRaw().toArray(new String[]{}));
    }
}
