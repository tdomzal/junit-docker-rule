package pl.domzal.junit.docker.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.NetworkSettings;
import com.spotify.docker.client.messages.PortBinding;

import pl.domzal.junit.wait.WaitForUnit;
import pl.domzal.junit.wait.WaitForUnit.WaitForCondition;

/**
 * Simple docker container junit {@link Rule}.
 * Inspired by and loosely based on osheeshel/DockerContainerRule - see {@link https://gist.github.com/mosheeshel/c427b43c36b256731a0b}.
 */
public class DockerRule extends ExternalResource {

    private static Logger log = LoggerFactory.getLogger(DockerRule.class);

    private final DockerClient dockerClient;
    private ContainerCreation container;

    private Map<String, List<PortBinding>> containerPorts;

    public DockerRule(DockerRuleBuiler builder) {

        HostConfig hostConfig = HostConfig.builder()//
                .portBindings(hostPortBindings(builder.getExposedPorts()))//
                .extraHosts(builder.getExtraHosts())//
                .build();

        ContainerConfig containerConfig = ContainerConfig.builder()//
                .hostConfig(hostConfig)//
                .image(builder.getImageName())//
                .networkDisabled(false)//
                .hostname("bleble:127.0.0.1")//
                .cmd(builder.getCmd()).build();

        try {

            //dockerClient = new DefaultDockerClient(DOCKER_SERVICE_URL);
            dockerClient = DefaultDockerClient.fromEnv().build();

            //TODO check if image is available (based of flag in builder ?)
            //dockerClient.pull(imageName);

            container = dockerClient.createContainer(containerConfig);

        } catch (DockerException | InterruptedException | DockerCertificateException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class DockerRuleBuiler {

        private String imageName;
        private String[] cmd;
        private String[] exposedPorts;
        private String[] extraHosts;
        private String waitFor;

        private DockerRuleBuiler(){}

        public DockerRule build() {
            return new DockerRule(this);
        }

        private static String[] nullToEmpty(String[] value) {
            return value==null ? new String[0] : value;
        }

        /**
         * Command to execute on container.
         */
        public DockerRuleBuiler setCmd(String... cmd) {
            this.cmd = cmd;
            return this;
        }
        public String[] getCmd() {
            return nullToEmpty(cmd);
        }

        /**
         * Image name to be used (required).
         */
        public DockerRuleBuiler setImageName(String imageName) {
            this.imageName = imageName;
            return this;
        }
        public String getImageName() {
            if (StringUtils.isEmpty(imageName)) {
                throw new IllegalStateException("imageName cannot be empty");
            }
            return imageName;
        }

        /**
         * Container ports to expose on host.
         * Port specific container port was mapped to can be later retreived with {@link DockerRule#getExposedContainerPort(String)}.
         */
        public DockerRuleBuiler setExposedPorts(String... exposedPorts) {
            this.exposedPorts = exposedPorts;
            return this;
        }
        public String[] getExposedPorts() {
            return nullToEmpty(exposedPorts);
        }

        /**
         * Add extra host definitions into containers <code>/etc/hosts</code>.
         * @param extraHosts List of host matching format "hostname:address" (like desribed for 'docker run --add-host').
         */
        public DockerRuleBuiler setExtraHosts(String... extraHosts) {
            this.extraHosts = extraHosts;
            return this;
        }
        public String[] getExtraHosts() {
            return nullToEmpty(extraHosts);
        }

        /**
         * Make rule to wait for specified text in log on container start.
         */
        public DockerRuleBuiler setWaitFor(String waitFor) {
            this.waitFor = waitFor;
            return this;
        }
        public String getWaitFor() {
            return waitFor;
        }
    }

    public static DockerRuleBuiler builder() {
        return new DockerRuleBuiler();
    }

    private static Map<String, List<PortBinding>> hostPortBindings(String[] exposedContainerPorts) {
        final Map<String, List<PortBinding>> hostPortBindings = new HashMap<String, List<PortBinding>>();
        for (String port : exposedContainerPorts) {
            List<PortBinding> hostPorts = new ArrayList<PortBinding>();
            hostPorts.add(PortBinding.of("0.0.0.0", port + "/tcp"));
            hostPortBindings.put(port + "/tcp", hostPorts);
        }
        return hostPortBindings;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return super.apply(base, description);
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        dockerClient.startContainer(container.id());
        log.debug("{} started", container.id());
        ContainerInfo inspectContainer = dockerClient.inspectContainer(container.id());
        log.debug("{} inspect", container.id());
        containerPorts = inspectContainer.networkSettings().ports();

        logMappings(dockerClient);
    }

    @Override
    protected void after() {
        super.after();
        try {
            ContainerState state = dockerClient.inspectContainer(container.id()).state();
            log.debug("container state: {}", state);
            if (state.running()) {
                dockerClient.killContainer(container.id());
                log.debug("{} killed", container.id());
            }
            dockerClient.removeContainer(container.id(), true);
            log.debug("{} removed", container.id());
        } catch (DockerException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    protected final String getDockerHost() {
        return dockerClient.getHost();
    }

    /**
     * Get host port conteiner internal port was mapped to.
     *
     * @param containerPort Container port. Typically it matches Dockerfile EXPOSE directive.
     * @return Host port conteiner port is exposed on.
     */
    protected final String getExposedContainerPort(String containerPort) {
        String key = containerPort + "/tcp";
        List<PortBinding> list = containerPorts.get(key);
        if (list == null || list.size() == 0) {
            throw new IllegalStateException(String.format("%s is not exposed", key));
        }
        if (list.size() == 0) {
            throw new IllegalStateException(String.format("binding list for %s is empty", key));
        }

        if (list.size() > 1) {
            throw new IllegalStateException(String.format("binding list for %s is longer than 1", key));
        }
        return list.get(0).hostPort();
    }

    private void logMappings(DockerClient dockerClient) throws DockerException, InterruptedException {
        ContainerInfo inspectContainer = dockerClient.inspectContainer(container.id());
        NetworkSettings networkSettings = inspectContainer.networkSettings();
        log.info("exposed ports: {}", networkSettings.ports());
    }

    public void waitFor(final String searchString, int waitTime) throws TimeoutException, InterruptedException {
        new WaitForUnit(TimeUnit.SECONDS, waitTime, TimeUnit.SECONDS, 1, new WaitForCondition() {
            @Override
            public boolean isConditionMet() {
                return StringUtils.contains(fullLogContent(), searchString);
            }

            @Override
            public String tickMessage() {
                return String.format("wait for '%s' in log", searchString);
            }

            @Override
            public String timeoutMessage() {
                return String.format("container log: \n%s", fullLogContent());
            }

        }) //
        .setLogLevelBeginEnd(Level.DEBUG) //
        .setLogLevelProgress(Level.TRACE) //
        .startWaiting();
    }

    String fullLogContent() {
        try (LogStream stream = dockerClient.logs(container.id(), LogsParam.stdout(), LogsParam.stderr());) {
            String fullLog = stream.readFully();
            if (log.isTraceEnabled()) {
                log.trace("{} full log: {}", container.id(), StringUtils.replace(fullLog, "\n", "|"));
            }
            return fullLog;
        } catch (DockerException | InterruptedException e) {
            throw new IllegalStateException(e);
        }

    }

    String getContainerId() {
        return container.id();
    }

    DockerClient getDockerClient() {
        return dockerClient;
    }

}
