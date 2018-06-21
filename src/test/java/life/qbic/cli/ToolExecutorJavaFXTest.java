package life.qbic.cli;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
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
import javafx.application.Platform;
import javafx.stage.Stage;
import life.qbic.exceptions.ApplicationException;
import life.qbic.javafx.QBiCApplication;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.hamcrest.CoreMatchers;
import org.junit.*;
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
 * It is possible to test JavaFX applications using monocle (http://mvnrepository.com/artifact/org.testfx/openjfx-monocle), but the purpose of these unit tests
 * is not to test the JavaFX framework (ToolExecutor uses Application.launch).
 */
public class ToolExecutorJavaFXTest {

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

        toolExecutor.invokeAsJavaFX(null, MockCommand.class, defaultArgs);
    }

    @Test
    public void testNullCommandClass() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("commandClass is required and cannot be null");

        toolExecutor.invokeAsJavaFX(MockApplication.class, null, defaultArgs);
    }

    @Test
    public void testNullArguments() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("args is required and cannot be null");

        toolExecutor.invokeAsJavaFX(MockApplication.class, MockCommand.class, null);
    }

    @Ignore(value = "https://github.com/qbicsoftware/core-utils-lib/issues/3")
    @Test
    public void testWithEmptyToolProperties() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_empty");
        toolExecutor.invokeAsJavaFX(MockApplication.class, MockCommand.class, defaultArgs);

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
        toolExecutor.invokeAsJavaFX(MockApplication.class, MockCommand.class, generateArguments("-f", "-h"));

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Help requested"));
    }

    @Test
    public void testHelpRequestedUsingLongOption() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");
        toolExecutor.invokeAsJavaFX(MockApplication.class, MockCommand.class, generateArguments("-f", "--help"));

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Help requested"));
    }

    @Test
    public void testVersionRequestedUsingShortOption() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");
        toolExecutor.invokeAsJavaFX(MockApplication.class, MockCommand.class, generateArguments("-f", "-v"));

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Version requested"));
    }

    @Test
    public void testVersionRequestedUsingLongOption() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");
        toolExecutor.invokeAsJavaFX(MockApplication.class, MockCommand.class, generateArguments("-f", "--version"));

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Version requested"));
    }

    @Test
    public void testVersionAndHelpRequested() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");
        toolExecutor.invokeAsJavaFX(MockApplication.class, MockCommand.class, generateArguments("-f", "-h", "-v"));

        // picocli outputs usage to System.out, but we can at least test something similar using our logger
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Help requested"));
        Mockito.verify(mockLogger).debug(ArgumentMatchers.contains("Version requested"));
    }

    @Test
    public void testWithPrivateConstructorInCommandClass() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        thrown.expect(ApplicationException.class);
        thrown.expectMessage(CoreMatchers.containsString("Could not find a no-arguments public constructor for the given command"));

        toolExecutor.invokeAsJavaFX(UselessApplicationForPrivateConstructorCommand.class, PrivateConstructorCommand.class, defaultArgs);
    }

    @Test
    public void testWithFaultyConstructorInCommandClass() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        thrown.expect(ApplicationException.class);
        thrown.expectMessage(CoreMatchers.containsString("Could not create a new instance of the command"));

        toolExecutor.invokeAsJavaFX(UselessApplicationForFaultyConstructorCommand.class, FaultyConstructorCommand.class, defaultArgs);
    }

    @Test
    public void testWithMissingDefaultConstructorInCommandClass() throws IOException, URISyntaxException {
        copyPropertiesFrom("tool.properties_fine");

        thrown.expect(ApplicationException.class);
        thrown.expectMessage(CoreMatchers.containsString("Could not find a no-arguments public constructor for the given command"));

        toolExecutor.invokeAsJavaFX(UselessApplicationForNoDefaultConstructorCommand.class, NoDefaultConstructorCommand.class, defaultArgs);
    }

    // ========== support methods/classes ============
    public static class ToolStatus {

        private final int key;
        private boolean completed;

        public ToolStatus(final int key) {
            this.key = key;
            completed = false;
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
        name = "ToolExecutorJavaFXTest",
        description = "Something something agile.")
    public static class MockCommand extends AbstractCommand {

        @Option(names = {"-f"}, description = "Faulty execution.")
        public volatile boolean faultyExecution;
        @Option(names = {"-k"}, description = "Key.", required = true)
        public volatile int key;

    }

    @Command(
        name = "ToolExecutorJavaFXTest",
        description = "Something something agile.")
    public static class NoDefaultConstructorCommand extends AbstractCommand {

        public NoDefaultConstructorCommand(final String something, final int unused) {

        }

    }

    @Command(
        name = "ToolExecutorJavaFXTest",
        description = "Something something agile.")
    public static class PrivateConstructorCommand extends AbstractCommand {

        private PrivateConstructorCommand() {

        }

    }

    @Command(
        name = "ToolExecutorJavaFXTest",
        description = "Something something agile.")
    public static class FaultyConstructorCommand extends AbstractCommand {

        public FaultyConstructorCommand() {
            throw new ApplicationException("command ctor() stop! hammertime!");
        }

    }


    public static class MockApplication extends QBiCApplication<MockCommand> {

        @Override
        public void start(final Stage stage) throws Exception {
            final MockCommand command = getCommand(MockCommand.class);
            final ToolStatus toolStatus = TOOL_STATUS_MAP.get(command.key);
            Validate.notNull(toolStatus,
                "It seems that this unit test was not set-up properly. A toolStatus is needed in TOOL_STATUS_MAP before executing each tool.");
            if (command.faultyExecution) {
                throw new ApplicationException("nope");
            }
            Platform.exit();
            toolStatus.completed = true;
        }

    }

    public static class UselessApplicationForFaultyConstructorCommand extends QBiCApplication<FaultyConstructorCommand> {

        @Override
        public void start(final Stage stage) throws Exception {
            getCommand(FaultyConstructorCommand.class);
            Platform.exit();
        }
    }

    public static class UselessApplicationForNoDefaultConstructorCommand extends QBiCApplication<NoDefaultConstructorCommand> {


        @Override
        public void start(final Stage stage) throws Exception {
            getCommand(NoDefaultConstructorCommand.class);
            Platform.exit();
        }
    }

    public static class UselessApplicationForPrivateConstructorCommand extends QBiCApplication<PrivateConstructorCommand> {

        @Override
        public void start(final Stage stage) throws Exception {
            getCommand(PrivateConstructorCommand.class);
            Platform.exit();
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
