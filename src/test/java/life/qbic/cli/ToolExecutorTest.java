package life.qbic.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import life.qbic.exceptions.ApplicationException;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Tests for ToolExecutor using {@link QBiCTool} instances.
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

    private ToolExecutor toolExecutor;
    private String[] defaultArgs;
    private ToolStatus toolStatus;

    private static Map<Integer, ToolStatus> TOOL_STATUS_MAP;
    private static AtomicInteger KEY;


    @BeforeClass
    public static void loggerSetup() {
        System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
        System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
        TOOL_STATUS_MAP = new ConcurrentHashMap<Integer, ToolStatus>();
        KEY = new AtomicInteger();
    }

    @Before
    public void setUpTest() {
        // inject mock logger
        Whitebox.setInternalState(ToolExecutor.class, "LOG", mockLogger);
        // init support instances
        toolExecutor = new ToolExecutor();
        final int currentKey = KEY.getAndIncrement();
        defaultArgs = new String[]{"-k", Integer.toString(currentKey)};
        toolStatus = new ToolStatus(currentKey);
        TOOL_STATUS_MAP.put(currentKey, toolStatus);
    }

    @After
    public void tearDown() throws IOException, URISyntaxException {
        deleteToolProperties();
    }

    @Test
    public void testNullToolClass() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("toolClass is required and cannot be null");

        toolExecutor.invoke(null, MockCommand.class, defaultArgs);
    }

    @Test
    public void testNullCommandClass() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("commandClass is required and cannot be null");

        toolExecutor.invoke(MockTool.class, null, defaultArgs);
    }

    @Test
    public void testNullArguments() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("args is required and cannot be null");

        toolExecutor.invoke(MockTool.class, MockCommand.class, null);
    }

    @Test
    public void testExecutionWithoutToolPropertiesFile() {
        toolExecutor.invoke(MockTool.class, MockCommand.class, defaultArgs);

        assertTrue("Tools are expected to execute even without a tool.properties", toolStatus.completed);
    }

    @Test
    public void testMissingToolPropertiesGeneratesWarnings() {
        toolExecutor.invoke(MockTool.class, MockCommand.class, defaultArgs);

        Mockito.verify(mockLogger).warn(ArgumentMatchers.contains("Missing tool descriptor"), ArgumentMatchers.anyString());
    }

    @Test
    public void testWithStrangeToolProperties() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_strange");
        toolExecutor.invoke(MockTool.class, MockCommand.class, defaultArgs);

        assertTrue("Tools are expected to execute even with an invalid tool.properties", toolStatus.completed);
        Mockito.verify(mockLogger)
            .warn(ArgumentMatchers.contains("Missing value in tool.properties file"), ArgumentMatchers.eq("tool.name"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testMissingToolName() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_noname");
        toolExecutor.invoke(MockTool.class, MockCommand.class, defaultArgs);

        assertTrue("Tools are expected to execute even with an incomplete tool.properties", toolStatus.completed);
        Mockito.verify(mockLogger)
            .warn(ArgumentMatchers.contains("Missing value in tool.properties file"), ArgumentMatchers.eq("tool.name"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testMissingToolVersion() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_noversion");
        toolExecutor.invoke(MockTool.class, MockCommand.class, defaultArgs);

        assertTrue("Tools are expected to execute even with an incomplete tool.properties", toolStatus.completed);
        Mockito.verify(mockLogger)
            .warn(ArgumentMatchers.contains("Missing value in tool.properties file"), ArgumentMatchers.eq("tool.version"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testMissingToolRepo() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_norepo");
        toolExecutor.invoke(MockTool.class, MockCommand.class, defaultArgs);

        assertTrue("Tools are expected to execute even with an incomplete tool.properties", toolStatus.completed);
        Mockito.verify(mockLogger)
            .warn(ArgumentMatchers.contains("Missing value in tool.properties file"), ArgumentMatchers.eq("tool.repo.url"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testWithEmptyToolProperties() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_empty");
        toolExecutor.invoke(MockTool.class, MockCommand.class, defaultArgs);

        assertTrue("Tools are expected to execute even with an incomplete tool.properties", toolStatus.completed);
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
    public void testHelpRequestedUsingShortOption() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");
        toolExecutor.invoke(MockTool.class, MockCommand.class, generateArguments("-f", "-s", "-h"));

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Help requested"));
    }

    @Test
    public void testHelpRequestedUsingLongOption() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");
        toolExecutor.invoke(MockTool.class, MockCommand.class, generateArguments("-f", "-s", "--help"));

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Help requested"));
    }

    @Test
    public void testVersionRequestedUsingShortOption() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");
        toolExecutor.invoke(MockTool.class, MockCommand.class, generateArguments("-f", "-s", "-v"));

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Version requested"));
    }

    @Test
    public void testVersionRequestedUsingLongOption() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");
        toolExecutor.invoke(MockTool.class, MockCommand.class, generateArguments("-f", "-s", "--version"));

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Version requested"));
    }

    @Test
    public void testVersionAndHelpRequested() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");
        toolExecutor.invoke(MockTool.class, MockCommand.class, generateArguments("-f", "-s", "-h", "-v"));

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Help requested"));
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Version requested"));
    }

    @Test
    public void testFaultyExecution() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            assertFalse(toolStatus.completed);
            assertFalse(toolStatus.active);
            assertTrue(toolStatus.shutdownInvoked);
            Mockito.verify(mockLogger).error(ArgumentMatchers.contains("execute() Stop! Hammertime!"));
        });

        toolExecutor.invoke(MockTool.class, MockCommand.class, generateArguments("-f"));
    }

    @Test
    public void testFaultyExecutionWithFaultyShutdown() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            assertFalse(toolStatus.completed);
            assertTrue(toolStatus.active);
            assertTrue(toolStatus.shutdownInvoked);
            Mockito.verify(mockLogger).error(ArgumentMatchers.contains("execute() Stop! Hammertime!"));
            Mockito.verify(mockLogger).error(ArgumentMatchers.contains("shutdown() Stop! Hammertime!"));
        });

        toolExecutor.invoke(MockTool.class, MockCommand.class, generateArguments("-f", "-s"));
    }

    @Test
    public void testNormalExecution() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        toolExecutor.invoke(MockTool.class, MockCommand.class, defaultArgs);

        assertTrue(toolStatus.completed);
        assertFalse(toolStatus.shutdownInvoked);
    }

    @Test
    public void testWithPrivateConstructorInCommandClass() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        thrown.expect(ApplicationException.class);
        thrown.expectMessage(CoreMatchers.containsString("Could not find a no-arguments public constructor for the given command"));

        toolExecutor.invoke(UselessToolForPrivateConstructorCommand.class, PrivateConstructorCommand.class, defaultArgs);
    }

    @Test
    public void testWithFaultyConstructorInCommandClass() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        thrown.expect(ApplicationException.class);
        thrown.expectMessage(CoreMatchers.containsString("Could not create a new instance of the command"));

        toolExecutor.invoke(UselessToolForFaultyConstructorCommand.class, FaultyConstructorCommand.class, defaultArgs);
    }

    @Test
    public void testWithMissingDefaultConstructorInCommandClass() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        thrown.expect(ApplicationException.class);
        thrown.expectMessage(CoreMatchers.containsString("Could not find a no-arguments public constructor for the given command"));

        toolExecutor.invoke(UselessToolForNoDefaultConstructorCommand.class, NoDefaultConstructorCommand.class, defaultArgs);
    }


    @Test
    public void testWithPrivateConstructorInToolClass() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        thrown.expect(ApplicationException.class);
        thrown.expectMessage(CoreMatchers.containsString("Could not find a suitable public constructor for the given tool"));

        toolExecutor.invoke(PrivateConstructorTool.class, MockCommand.class, defaultArgs);
    }


    @Test
    public void testWithFaultyConstructorInToolClass() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        thrown.expect(ApplicationException.class);
        thrown.expectMessage(CoreMatchers.containsString("Could not create a new instance for the given tool"));

        toolExecutor.invoke(FaultyConstructorTool.class, MockCommand.class, defaultArgs);
    }

    @Test
    public void testWithMissingConstructorInToolClass() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        thrown.expect(ApplicationException.class);
        thrown.expectMessage(CoreMatchers.containsString("Could not find a suitable public constructor for the given tool"));

        toolExecutor.invoke(MissingConstructorTool.class, MockCommand.class, defaultArgs);
    }

    @Ignore(value = "Ignoring testFaultyShutdown. There doesn't seem to be a simple and reasonable way to test shutdown hooks, for now.")
    @Test
    public void testFaultyShutdown() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        exit.expectSystemExit();
        exit.checkAssertionAfterwards(() -> {
            assertTrue(toolStatus.completed);
            assertTrue(toolStatus.active);
            assertTrue(toolStatus.shutdownInvoked);
            Mockito.verify(mockLogger).error(ArgumentMatchers.contains("shutdown() Stop! Hammertime!"));
        });

        // force the JVM to invoke our shutdown method
        toolExecutor.invoke(MockTool.class, MockCommand.class, generateArguments("-s"));
        System.exit(0);
    }

    @Test
    public void testWithPrivateMembersInCommand() throws IOException, URISyntaxException {
        // I was curious about how picocli implements it and kind of lost in the code, so adding this test
        copyPropertiesFrom("tool.properties_fine");

        toolExecutor.invoke(MockTool.class, MockCommand.class, generateArguments("-p", "1234"));

        assertEquals(1234, toolStatus.command.privateParameter);
    }

    // ========== support methods/classes ===========
    public static class ToolStatus {

        private final int key;
        private volatile boolean completed;
        private volatile boolean active;
        private volatile boolean shutdownInvoked;
        private volatile MockCommand command;

        public ToolStatus(final int key) {
            this.key = key;
            completed = active = shutdownInvoked = false;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final ToolStatus that = (ToolStatus) o;
            return Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return key;
        }
    }

    @Command(
        name = "ToolExecutorTest",
        description = "Something something agile.")
    public static class MockCommand extends AbstractCommand {

        @Option(names = {"-f"}, description = "Faulty execution.")
        public volatile boolean faultyExecution;
        @Option(names = {"-s"}, description = "Faulty Shutdown.")
        public volatile boolean faultyShutdown;
        @Option(names = {"-k"}, description = "Key.", required = true)
        public volatile int key;

        @Option(names = {"-p", "--parameter"})
        private int privateParameter;

    }

    @Command(
        name = "ToolExecutorTest",
        description = "Something something agile.")
    public static class NoDefaultConstructorCommand extends AbstractCommand {

        public NoDefaultConstructorCommand(final String something, final int unused) {

        }

    }

    @Command(
        name = "ToolExecutorTest",
        description = "Something something agile.")
    public static class PrivateConstructorCommand extends AbstractCommand {

        private PrivateConstructorCommand() {

        }

    }

    @Command(
        name = "ToolExecutorTest",
        description = "Something something agile.")
    public static class FaultyConstructorCommand extends AbstractCommand {

        public FaultyConstructorCommand() {
            throw new ApplicationException("command ctor() stop! hammertime!");
        }

    }


    public static class MockTool extends QBiCTool<MockCommand> {

        private final ToolStatus toolStatus;

        public MockTool(final MockCommand command) {
            super(command);
            toolStatus = TOOL_STATUS_MAP.get(command.key);
            Validate.notNull(toolStatus,
                "It seems that this unit test was not set-up properly. A toolStatus is needed in TOOL_STATUS_MAP before executing each tool.");
            toolStatus.command = command;
        }

        @Override
        public void execute() {
            toolStatus.active = true;
            if (super.getCommand().faultyExecution) {
                throw new ApplicationException("execute() Stop! Hammertime!");
            }
            toolStatus.completed = true;
        }

        @Override
        public void shutdown() {
            toolStatus.shutdownInvoked = true;
            if (super.getCommand().faultyShutdown) {
                throw new ApplicationException("shutdown() Stop! Hammertime!");
            }
            toolStatus.active = false;
        }
    }

    public static class PrivateConstructorTool extends QBiCTool<MockCommand> {

        private PrivateConstructorTool(final MockCommand command) {
            super(command);
        }

        @Override
        public void execute() {

        }
    }

    public static class FaultyConstructorTool extends QBiCTool<MockCommand> {

        public FaultyConstructorTool(final MockCommand command) {
            super(command);
            throw new ApplicationException("tool ctor() stop! hammertime!");
        }

        @Override
        public void execute() {

        }
    }

    public static class MissingConstructorTool extends QBiCTool<MockCommand> {

        public MissingConstructorTool() {
            super(new MockCommand());
        }

        @Override
        public void execute() {

        }
    }

    public static class UselessToolForFaultyConstructorCommand extends QBiCTool<FaultyConstructorCommand> {

        public UselessToolForFaultyConstructorCommand(final FaultyConstructorCommand command) {
            super(command);
        }

        @Override
        public void execute() {

        }
    }

    public static class UselessToolForNoDefaultConstructorCommand extends QBiCTool<NoDefaultConstructorCommand> {

        private UselessToolForNoDefaultConstructorCommand(final NoDefaultConstructorCommand command) {
            super(command);
        }

        @Override
        public void execute() {

        }
    }

    public static class UselessToolForPrivateConstructorCommand extends QBiCTool<PrivateConstructorCommand> {

        private UselessToolForPrivateConstructorCommand(final PrivateConstructorCommand command) {
            super(command);
        }

        @Override
        public void execute() {

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

    private String[] generateArguments(final String... args) {
        Validate
            .notNull(toolStatus, "It seems that this unit test was not set-up properly- A non-null toolStatus is needed to generate custom arguments.");
        // add "-k" and currentKey
        final String[] allArgs = Arrays.copyOf(args, args.length + 2);
        allArgs[args.length] = "-k";
        allArgs[args.length + 1] = Integer.toString(toolStatus.key);

        return allArgs;
    }

}