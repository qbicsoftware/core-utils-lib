package life.qbic.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import life.qbic.exceptions.ApplicationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

/**
 * Class that coordinates execution of generic command-line tools.
 *
 * In order to be able to write "generic" code that can run "any" tool, we have decided to use the Strategy design pattern. This class represents the
 * <i>Context</i> (i.e., command-line tools and services).
 */
public class ToolExecutor<T extends AbstractCommand> {

    private static final Logger LOG = LogManager.getLogger(ToolExecutor.class);
    // package access for testing purposes
    static final String TOOL_PROPERTIES_PATH = "tool.properties";
    static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT";
    static final String DEFAULT_REPO = "http://github.com/qbicsoftware";
    static final String DEFAULT_NAME = "QBiC toolset";

    /**
     * Invokes the given tool.
     */
    public void invoke(final Tool<T> tool, final T command) {
        Validate.notNull(tool, "parameter tool is required and cannot be null");
        Validate.notNull(command, "parameter command is required and cannot be null");

        final Properties properties = new Properties();
        try (final InputStream inputStream = ToolExecutor.class.getClassLoader().getResourceAsStream(TOOL_PROPERTIES_PATH)) {
            if (inputStream == null) {
                LOG.warn("Missing tool descriptor file. Make sure the file {} is located in the classpath", TOOL_PROPERTIES_PATH);
            } else {
                properties.load(inputStream);
            }
        } catch (final IOException e) {
            throw new ApplicationException("Could not load required file tool.properties. Make sure tool.properties can be found in the classpath.", e);
        }
        // optional properties
        final String toolVersion = extractAndWarnIfMissing(properties, "tool.version", DEFAULT_VERSION);
        final String toolRepositoryUrl = extractAndWarnIfMissing(properties, "tool.repo.url", DEFAULT_REPO);
        final String toolName = extractAndWarnIfMissing(properties, "tool.name", DEFAULT_NAME);

        // this is the only thing that is the same across all tools: --help and --version
        if (command.printVersion || command.printHelp) {
            if (command.printVersion) {
                LOG.debug("Version requested.");
                LOG.info("{}, version {} ({})", toolName, toolVersion, toolRepositoryUrl);
            }
            if (command.printHelp) {
                LOG.debug("Help requested.");
                CommandLine.usage(command, System.out);
            }
            return;
        }

        startTool(tool);
    }

    private void startTool(final Tool<T> tool) {
        final Lock shutdownAccessLock = new ReentrantLock();
        final AtomicBoolean cleanShutdown = new AtomicBoolean(false);
        // this is where the "strategy" design pattern pays off; Tool developers need only to implement two methods: execute and shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdownTool(tool, shutdownAccessLock, cleanShutdown);
            } catch (final Exception e) {
                logException(e);
                // calling System.exit while processing shutdown hooks should not be done,
                // see: https://docs.oracle.com/javase/8/docs/api/java/lang/Runtime.html#exit-int-
            }
        }));

        LOG.debug("Starting execution");
        try {
            tool.execute();
        } catch (final Exception e) {
            logException(e);
            shutdownTool(tool, shutdownAccessLock, cleanShutdown);
            System.exit(1);
        }
        // do not invoke System.exit
        // let the JVM handle exiting normally, the tool could be a daemon
    }

    private void shutdownTool(final Tool<T> tool, final Lock shutdownAccessLock, final AtomicBoolean cleanShutdown) {
        shutdownAccessLock.lock();
        try {
            if (!cleanShutdown.get()) {
                LOG.debug("Shutting down");
                tool.shutdown();
                cleanShutdown.set(true);
            } else {
                LOG.debug("Tool has already been shutdown, ignoring request.");
            }
        } catch (final Exception e) {
            // we are shutting down, just log the exceptions
            logException(e);
        } finally {
            shutdownAccessLock.unlock();
        }
    }

    private static void logException(final Exception e) {
        LOG.error(e.getMessage());
        LOG.error("Check the application log in logs/app.log for more details.");
        LOG.debug("Full stack trace: ", e);
    }


    private static String extractAndWarnIfMissing(final Properties properties, final String key, final String defaultValue) {
        String value = StringUtils.trimToEmpty(properties.getProperty(key));
        if (StringUtils.isBlank(value)) {
            LOG.warn("Missing value in tool.properties file for property '{}', using default value '{}'", key, defaultValue);
            value = defaultValue;
        }
        return value;
    }
}