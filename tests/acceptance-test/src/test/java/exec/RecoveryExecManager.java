package exec;

import com.quorum.tessera.launcher.Main;
import config.ConfigDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suite.ExecutionContext;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecoveryExecManager implements ExecManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryExecManager.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final URL logbackConfigFile = RecoveryExecManager.class.getResource("/logback-node.xml");

    private ConfigDescriptor configDescriptor;

    private final Path pid;

    private final String nodeId;

    public RecoveryExecManager(ConfigDescriptor configDescriptor) {
        this.configDescriptor = configDescriptor;
        this.pid =
            Paths.get(System.getProperty("java.io.tmpdir"), "recoverynode-" + configDescriptor.getAlias().name() + ".pid");
        this.nodeId = suite.NodeId.generate(ExecutionContext.currentContext(), configDescriptor.getAlias());
    }

    @Override
    public Process doStart() throws Exception {

        Path nodeServerJar =
            Paths.get(
                System.getProperty(
                    "application.jar", "../../tessera-app/target/tessrea-app-0.10-SNAPSHOT-app.jar"));

        ExecutionContext executionContext = ExecutionContext.currentContext();

        ExecArgsBuilder argsBuilder =
            new ExecArgsBuilder()
                .withArg("--recover")
                .withJvmArg("-Dnode.number=" + nodeId.concat("-").concat("recover"))
                .withStartScriptOrJarFile(nodeServerJar)
                .withMainClass(Main.class)
                .withPidFile(pid)
                .withConfigFile(configDescriptor.getPath())
                .withJvmArg("-Dlogback.configurationFile=" + logbackConfigFile.getFile())
                .withClassPathItem(nodeServerJar);

        List<String> args = argsBuilder.build();

        Map<String, String> env = Collections.EMPTY_MAP;
        final Process process = ExecUtils.start(args, executorService, env);

        return process;
    }

    @Override
    public void doStop() throws Exception {
        String p = Files.lines(pid).findFirst().orElse(null);
        if (p == null) {
            return;
        }
        LOGGER.info("Stopping Node: {}, Pid: {}", nodeId, p);
        try {
            ExecUtils.kill(p);
        } finally {
            executorService.shutdown();
        }
    }

    @Override
    public ConfigDescriptor getConfigDescriptor() {
        return configDescriptor;
    }
}
