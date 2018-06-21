package life.qbic.cli;

import org.apache.commons.lang3.Validate;

/**
 * Tools implementations derive from this base class.
 *
 * @param <T> The command class used by tool implementations to represent the already parsed command-line arguments.
 */
public abstract class QBiCTool<T extends AbstractCommand> implements Tool {

    private final T command;

    /**
     * Constructor.
     *
     * @param command The command representing the given command-line arguments.
     */
    public QBiCTool(final T command) {
        Validate.notNull(command, "command is required and cannot be null");
        this.command = command;
    }

    @Override
    public void shutdown() {
        // default shutdown implementation
    }

    /**
     * @return a copy of the parsed command-line argument provided when creating this instance.
     */
    protected final T getCommand() {
        return command;
    }
}
