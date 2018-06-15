package life.qbic.cli;

import org.apache.commons.lang3.Validate;

public abstract class AbstractTool<T extends AbstractCommand> implements Tool<T> {

    protected final T command;

    public AbstractTool(final T command) {
        Validate.notNull(command, "command is required and cannot be null");
        this.command = command;
    }
}
