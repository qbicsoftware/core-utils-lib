package life.qbic.cli;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.application.Application;
import life.qbic.exceptions.ApplicationException;
import life.qbic.javafx.QBiCApplication;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

/**
 * Class that coordinates execution of generic command-line {@link QBiCTool}s and {@link QBiCApplication}s.
 *
 * In order to be able to write "generic" code that can run "any" tool, we have decided to use the Strategy design pattern. This class represents the
 * <i>Context</i> (i.e., command-line tools and services).
 */
public class ToolExecutor {

    private static final Logger LOG = LogManager.getLogger(ToolExecutor.class);
    // package access for testing purposes
    static final String TOOL_PROPERTIES_PATH = "tool.properties";
    static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT";
    static final String DEFAULT_REPO = "http://github.com/qbicsoftware";
    static final String DEFAULT_NAME = "QBiC toolset";

    /**
     * Invokes the given {@link QBiCTool}.
     *
     * @param toolClass the class of the tool to invoke.
     * @param commandClass the class of the commands that the tool is able to understand.
     * @param args the provided command-line arguments.
     */
    public <T extends AbstractCommand> void invoke(final Class<? extends QBiCTool<T>> toolClass, final Class<T> commandClass, final String[] args) {
        final AbstractCommand command = validateParametersAndParseCommandlineArguments(toolClass, commandClass, args);

        if (handleCommonParameters(extractToolMetadata(), command)) {
            return;
        }

        startQBiCTool(instantiateTool(toolClass, command));
    }

    /**
     * Invokes a {@link QBiCApplication}.
     *
     * @param applicationClass the class of the application to launch.
     * @param commandClass the class of the commands that the application is able to understand.
     * @param args the command-line arguments.
     */
    public void invokeAsJavaFX(final Class<? extends QBiCApplication> applicationClass, final Class<? extends AbstractCommand> commandClass,
        final String[] args) {
        final AbstractCommand command = validateParametersAndParseCommandlineArguments(applicationClass, commandClass, args);

        if (handleCommonParameters(extractToolMetadata(), command)) {
            return;
        }

        Application.launch(applicationClass, args);
    }

    private AbstractCommand validateParametersAndParseCommandlineArguments(final Class<?> toolClass, final Class<? extends AbstractCommand> commandClass,
        final String[] args) {
        Validate.notNull(toolClass, "toolClass is required and cannot be null");
        Validate.notNull(commandClass, "commandClass is required and cannot be null");
        Validate.notNull(args, "args is required and cannot be null");

        return AbstractCommand.parseArguments(commandClass, args);
    }

    private Tool instantiateTool(final Class<? extends QBiCTool> toolClass, final AbstractCommand command) {
        final Tool tool;
        final Constructor<? extends QBiCTool> constructor;

        try {
            constructor = toolClass.getConstructor(command.getClass());
        } catch (final NoSuchMethodException e) {
            throw new ApplicationException(String
                .format("Could not find a suitable public constructor for the given tool with class name %s. Check QBiCTool class' documentation.", toolClass),
                e);
        }

        try {
            tool = constructor.newInstance(command);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ApplicationException(String.format("Could not create a new instance for the given tool with class name %s", toolClass), e);
        }

        return tool;
    }

    // returns true if the common parameters were handled, meaning that
    private boolean handleCommonParameters(final ToolMetadata toolMetadata, final AbstractCommand command) {
        // this is the only thing that is the same across all tools: --help and --version
        if (command.printVersion || command.printHelp) {
            if (command.printVersion) {
                LOG.debug("Version requested.");
                LOG.info("{}, version {} ({})", toolMetadata.toolName, toolMetadata.toolVersion, toolMetadata.toolRepoUrl);
            }
            if (command.printHelp) {
                LOG.debug("Help requested.");
                CommandLine.usage(command, System.out);
            }
            return true;
        }
        return false;
    }

    private ToolMetadata extractToolMetadata() {
        final Properties properties = new Properties();
        try (final InputStream inputStream = ToolExecutor.class.getClassLoader().getResourceAsStream(TOOL_PROPERTIES_PATH)) {
            if (inputStream == null) {
                LOG.warn("Missing tool descriptor file. Make sure the file {} is located in the classpath", TOOL_PROPERTIES_PATH);
            } else {
                properties.load(inputStream);
            }
        } catch (final IOException e) {
            throw new ApplicationException(
                "Could not load required file tool.properties. Make sure tool.properties can be found in the classpath and that it is properly formatted.", e);
        }
        // optional properties
        final String toolVersion = extractAndWarnIfMissing(properties, "tool.version", DEFAULT_VERSION);
        final String toolRepositoryUrl = extractAndWarnIfMissing(properties, "tool.repo.url", DEFAULT_REPO);
        final String toolName = extractAndWarnIfMissing(properties, "tool.name", DEFAULT_NAME);

        return new ToolMetadata(toolName, toolVersion, toolRepositoryUrl);
    }

    private void startQBiCTool(final Tool tool) {
        final Lock shutdownAccessLock = new ReentrantLock();
        final AtomicBoolean cleanShutdown = new AtomicBoolean(false);
        // this is where the "strategy" design pattern pays off; Tool developers need only to implement two methods: execute and shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdownHook(tool, shutdownAccessLock, cleanShutdown);
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
            shutdownHook(tool, shutdownAccessLock, cleanShutdown);
            System.exit(1);
        }
        // do not invoke System.exit
        // let the JVM handle exiting normally, the tool could be a daemon
    }

    private void shutdownHook(final Tool tool, final Lock shutdownAccessLock, final AtomicBoolean cleanShutdown) {
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

    private class ToolMetadata {

        final String toolName;
        final String toolVersion;
        final String toolRepoUrl;

        ToolMetadata(final String toolName, final String toolVersion, final String toolRepoUrl) {
            this.toolName = toolName;
            this.toolVersion = toolVersion;
            this.toolRepoUrl = toolRepoUrl;
        }
    }
}