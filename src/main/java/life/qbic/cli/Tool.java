package life.qbic.cli;

/**
 * Interface that represents command-line tools and services such as daemons.
 *
 * In order to be able to write "generic" code that can run "any" tool, we have decided to use the <i>Strategy</i> design pattern. This interface represents all
 * available <i>strategies</i> (i.e., command-line tools and services).
 *
 * JavaFX provides a framework to start JavaFX applications (i.e., {@link javafx.application.Application#launch(String...)}), but to separate JavaFX code
 * from the non-JavaFX code, we refactored all JavaFX-related code to {@link http://github.com/qbicsoftware/javafx-utils-lib}.
 *
 * @see {@link ToolExecutor}
 * @see {@link AbstractCommand}
 * @see {@link QBiCTool}
 */
public interface Tool {

    /**
     * Implementations should use this method as the "main" method.
     */
    void execute();

    /**
     * Invoked before the virtual machine is about to shutdown.
     */
    void shutdown();
}
