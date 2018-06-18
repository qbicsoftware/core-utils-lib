package life.qbic.cli;

import org.apache.commons.lang3.Validate;

/**
 * Tools implementations derive from this base class.
 *
 * @param <T> The command class used by tool implementations to represent the already parsed command-line arguments.
 */
public abstract class AbstractTool<T extends AbstractCommand> implements Tool<T> {

    protected final T command;

    /**
     * Constructor.
     *
     * @param command The command representing the given command-line arguments.
     */
    public AbstractTool(final T command) {
        Validate.notNull(command, "command is required and cannot be null");
        this.command = command;
    }

    @Override
    public void shutdown() {
        // provides a default implementation
    }
}
