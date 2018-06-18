package life.qbic.cli;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import life.qbic.exceptions.ApplicationException;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import picocli.CommandLine.Command;

/**
 * Tests for ToolExecutor.
 */
public class ToolExecutorTest {

    @Mock
    private Logger mockLogger;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private CommandMock command;
    private ToolExecutor<CommandMock> toolExecutor;

    @BeforeClass
    public static void loggerSetup() {
        System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
        System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
    }

    @Before
    public void setUpTest() {
        // inject mock logger
        Whitebox.setInternalState(ToolExecutor.class, "LOG", mockLogger);
        // init support instances
        command = new CommandMock();
        command.printHelp = false;
        command.printVersion = false;
        toolExecutor = new ToolExecutor<>();
    }

    @After
    public void tearDown() throws IOException, URISyntaxException {
        deleteToolProperties();
    }

    @Test
    public void testNullTool() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("parameter tool is required and cannot be null");

        toolExecutor.invoke(null, command);
    }

    @Test
    public void testNullCommand() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("parameter command is required and cannot be null");

        toolExecutor.invoke(new ToolMock(false, false, command), null);
    }

    @Test
    public void testExecutionWithoutToolPropertiesFile() {
        final ToolMock tool = new ToolMock(false, false, command);

        toolExecutor.invoke(tool, command);

        assertTrue("Tools are expected to execute even without a tool.properties", tool.completed);
    }

    @Test
    public void testMissingToolPropertiesGeneratesWarnings() {
        final ToolMock tool = new ToolMock(false, false, command);

        toolExecutor.invoke(tool, command);

        Mockito.verify(mockLogger).warn(ArgumentMatchers.contains("Missing tool descriptor"), ArgumentMatchers.anyString());
    }

    @Test
    public void testMissingToolName() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_noname");
        final ToolMock tool = new ToolMock(false, false, command);

        toolExecutor.invoke(tool, command);

        Mockito.verify(mockLogger)
            .warn(ArgumentMatchers.contains("Missing value in tool.properties file"), ArgumentMatchers.eq("tool.name"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testMissingToolVersion() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_noversion");
        final ToolMock tool = new ToolMock(false, false, command);

        toolExecutor.invoke(tool, command);

        Mockito.verify(mockLogger)
            .warn(ArgumentMatchers.contains("Missing value in tool.properties file"), ArgumentMatchers.eq("tool.version"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testMissingToolRepo() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_norepo");
        final ToolMock tool = new ToolMock(false, false, command);

        toolExecutor.invoke(tool, command);

        Mockito.verify(mockLogger)
            .warn(ArgumentMatchers.contains("Missing value in tool.properties file"), ArgumentMatchers.eq("tool.repo.url"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testWithEmptyToolProperties() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_empty");
        final ToolMock tool = new ToolMock(false, false, command);

        toolExecutor.invoke(tool, command);

        Mockito.verify(mockLogger)
            .warn(ArgumentMatchers.contains("Missing value in tool.properties file"), ArgumentMatchers.eq("tool.name"),
                ArgumentMatchers.anyString());
        Mockito.verify(mockLogger)
            .warn(ArgumentMatchers.contains("Missing value in tool.properties file"), ArgumentMatchers.eq("tool.version"),
                ArgumentMatchers.anyString());
        Mockito.verify(mockLogger)
            .warn(ArgumentMatchers.contains("Missing value in tool.properties file"), ArgumentMatchers.eq("tool.repo.url"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testHelpRequested() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");
        command.printHelp = true;
        final Tool<CommandMock> tool = new ToolMock(true, true, command);

        toolExecutor.invoke(tool, command);

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Help requested"));
    }

    @Test
    public void testVersionRequested() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");
        command.printVersion = true;
        final Tool<CommandMock> tool = new ToolMock(true, true, command);

        toolExecutor.invoke(tool, command);

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Version requested"));
    }

    @Test
    public void testVersionAndHelpRequested() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");
        command.printHelp = true;
        command.printVersion = true;
        final Tool<CommandMock> tool = new ToolMock(true, true, command);

        toolExecutor.invoke(tool, command);

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Help requested"));
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Version requested"));
    }

    @Test
    public void testFaultyExecution() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        final ToolMock tool = new ToolMock(true, false, command);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                assertFalse(tool.completed);
                assertFalse(tool.active);
                assertTrue(tool.shutdownInvoked);
                Mockito.verify(mockLogger).error(ArgumentMatchers.contains("execute() Stop! Hammertime!"));
            }
        });

        toolExecutor.invoke(tool, command);
    }

    @Test
    public void testFaultyExecutionWithFaultyShutdown() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        final ToolMock tool = new ToolMock(true, true, command);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                assertFalse(tool.completed);
                assertTrue(tool.active);
                assertTrue(tool.shutdownInvoked);
                Mockito.verify(mockLogger).error(ArgumentMatchers.contains("execute() Stop! Hammertime!"));
                Mockito.verify(mockLogger).error(ArgumentMatchers.contains("shutdown() Stop! Hammertime!"));
            }
        });

        toolExecutor.invoke(tool, command);
    }

    @Test
    public void testNormalExecution() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        final ToolMock tool = new ToolMock(false, false, command);
        toolExecutor.invoke(tool, command);

        assertTrue(tool.completed);
        assertFalse(tool.shutdownInvoked);
    }

    @Ignore(value = "Ignoring testFaultyShutdown. There doesn't seem to be a simple and reasonable way to test shutdown hooks, for now.")
    @Test
    public void testFaultyShutdown() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        final ToolMock tool = new ToolMock(false, true, command);

        exit.expectSystemExit();
        exit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                assertTrue(tool.completed);
                assertTrue(tool.active);
                assertTrue(tool.shutdownInvoked);
                Mockito.verify(mockLogger).error(ArgumentMatchers.contains("shutdown() Stop! Hammertime!"));
            }
        });

        // force the JVM to invoke our shutdown method
        toolExecutor.invoke(tool, command);
        System.exit(0);
    }

    // ========== support methods/classes ============
    @Command(
        name = "ToolExecutorTest",
        description = "Something something agile.")
    private static class CommandMock extends AbstractCommand {

    }

    private static class ToolMock extends AbstractTool<CommandMock> {

        private final boolean faultyExecution;
        private final boolean faultyShutdown;
        private volatile boolean completed = false;
        private volatile boolean active = false;
        private volatile boolean shutdownInvoked = false;

        public ToolMock(final boolean faultyExecution, final boolean faultyShutdown, final CommandMock command) {
            super(command);
            this.faultyExecution = faultyExecution;
            this.faultyShutdown = faultyShutdown;
        }

        @Override
        public void execute() {
            active = true;
            if (faultyExecution) {
                throw new ApplicationException("execute() Stop! Hammertime!");
            }
            completed = true;
        }

        @Override
        public void shutdown() {
            shutdownInvoked = true;
            if (faultyShutdown) {
                throw new ApplicationException("shutdown() Stop! Hammertime!");
            }
            active = false;
        }
    }

    private void copyPropertiesFrom(final String propertiesFilePath)
        throws URISyntaxException, IOException {
        final Path source = Paths.get(getClass().getClassLoader().getResource(propertiesFilePath).toURI());
        final Path target = Paths.get(source.getParent().toString(), File.separator, ToolExecutor.TOOL_PROPERTIES_PATH);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private void deleteToolProperties() throws URISyntaxException, IOException {
        final URL toolPropertiesUrl = getClass().getClassLoader().getResource(ToolExecutor.TOOL_PROPERTIES_PATH);
        if (toolPropertiesUrl != null) {
            Files.deleteIfExists(Paths.get(toolPropertiesUrl.toURI()));
        }
    }

}