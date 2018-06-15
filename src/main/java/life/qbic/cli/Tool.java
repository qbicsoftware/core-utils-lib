package life.qbic.cli;

/**
 * Interface that represents command-line tools and services such as daemons.
 *
 * In order to be able to write "generic" code that can run "any" tool, we have decided to use the Strategy design pattern. This interface represents all
 * available <i>strategies</i> (i.e., command-line tools and services).
 *
 * @see {@link ToolExecutor}
 */
public interface Tool<T extends AbstractCommand> {

    /**
     * Implementations should use this method as the "main" method.
     *
     * @return the return code of the operation.
     */
    void execute();

    /**
     * Invoked before the virtual machine is about to shutdown.
     */
    void shutdown();
}
