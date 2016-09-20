package pl.domzal.junit.docker.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.PortBinding;

import pl.domzal.junit.docker.rule.ex.ImagePullException;
import pl.domzal.junit.docker.rule.ex.PortNotExposedException;
import pl.domzal.junit.docker.rule.wait.HttpPingChecker;
import pl.domzal.junit.docker.rule.wait.LineListener;
import pl.domzal.junit.docker.rule.wait.LogChecker;
import pl.domzal.junit.docker.rule.wait.LogSequenceChecker;
import pl.domzal.junit.docker.rule.wait.TcpPortChecker;

/**
 * Simple docker container junit {@link Rule}.<br/>
 * Instances should be created via builder:
 * <pre>
 *  &#064;Rule
 *  DockerRule container = DockerRule.builder()
 *      . //configuration directives
 *      .build();
 * </pre>
 * <br/>
 * Inspired by and loosely based on <a href="https://gist.github.com/mosheeshel/c427b43c36b256731a0b">osheeshel/DockerContainerRule</a>.
 */
public class DockerRule extends ExternalResource {

    private static Logger log = LoggerFactory.getLogger(DockerRule.class);

    private static final int STOP_TIMEOUT = 5;
    private static final int SHORT_ID_LEN = 12;

    private final DockerRuleBuilder builder;
    private final String imageNameWithTag;
    private final DockerClient dockerClient;

    private ContainerCreation container;
    private String containerShortId;
    private String containerIp;
    private String containerGateway;
    private Map<String, List<PortBinding>> containerPorts;
    
    private ContainerInfo containerInfo;

    private DockerLogs dockerLogs;

    private boolean isStarted = false;

    DockerRule(DockerRuleBuilder builder) {
        this.builder = builder;
        this.imageNameWithTag = imageNameWithTag(builder.imageName());
        try {
            dockerClient = DefaultDockerClient.fromEnv().build();
            log.debug("server.info: {}", dockerClient.info());
            log.debug("server.version: {}", dockerClient.version());
            if (builder.imageAlwaysPull() || ! imageAvaliable(dockerClient, imageNameWithTag)) {
                dockerClient.pull(imageNameWithTag);
            }
        } catch (ImageNotFoundException e) {
            throw new ImagePullException(String.format("Image '%s' not found", imageNameWithTag), e);
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Builder to specify parameters and produce {@link DockerRule} instance.
     */
    public static DockerRuleBuilder builder() {
        return new DockerRuleBuilder();
    }

    /**
     * Create and start container.<br/>
     * This is {@link ExternalResource#before()} made available as public - it may be helpful in scenarios
     * when you want to use {@link DockerRule} and operate it manually.
     */
    @Override
    public final void before() throws Throwable {
        HostConfig hostConfig = HostConfig.builder()//
                .publishAllPorts(builder.publishAllPorts())//
                .portBindings(builder.hostPortBindings())//
                .binds(builder.binds())//
                .links(links())//
                .extraHosts(builder.extraHosts())//
                .build();
        ContainerConfig containerConfig = ContainerConfig.builder()//
                .hostConfig(hostConfig)//
                .image(imageNameWithTag)//
                .env(builder.env())//
                .networkDisabled(false)//
                .exposedPorts(builder.containerExposedPorts())
                .entrypoint(builder.entrypoint())
                .labels(builder.getLabels())
                .cmd(builder.cmd()).build();
        try {
            if (StringUtils.isNotBlank(builder.name())) {
                this.container = dockerClient.createContainer(containerConfig, builder.name());
            } else {
                this.container = dockerClient.createContainer(containerConfig);
            }

            this.containerShortId = StringUtils.left(container.id(), SHORT_ID_LEN);
            log.info("container {} created, id {}, short id {}", imageNameWithTag, container.id(), containerShortId);
            log.debug("rule before {}", containerShortId);

            dockerClient.startContainer(container.id());
            log.debug("{} started", containerShortId);

            LogSequenceChecker lineListener = new LogSequenceChecker(builder.waitForMessageSequence());
            attachLogs(dockerClient, container.id(), lineListener);

            ContainerInfo containerInfo = dockerClient.inspectContainer(container.id());
            containerIp = containerInfo.networkSettings().ipAddress();
            containerPorts = containerInfo.networkSettings().ports();
            containerGateway = containerInfo.networkSettings().gateway();
            this.containerInfo = containerInfo;

            waitForStartConditions(lineListener);
            logNetworkSettings();

            isStarted = true;

        } catch (DockerRequestException e) {
            throw new IllegalStateException(e.message(), e);
        } catch (DockerException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isStarted() {
        return isStarted;
    }

    private List<String> links() {
        List<String> resolvedLinks = new ArrayList<>();
        resolvedLinks.addAll(builder.staticLinks());
        for (Pair<DockerRule,String> dynamicLink : builder.getDynamicLinks()) {
            DockerRule rule = dynamicLink.getKey();
            String alias = dynamicLink.getValue();
            if (!rule.isStarted()) {
                throw new IllegalStateException(String.format("container linked via alias '%s' is not started, make sure rule definitions assures target container will be started first", alias));
            }
            resolvedLinks.add(rule.getContainerId() + ":" + alias);
        }
        return resolvedLinks;
    }

    // TODO refactor out waitFor logic to external class
    private void waitForStartConditions(LogSequenceChecker lineListener) throws TimeoutException, InterruptedException {
        if (!builder.waitForMessageSequence().isEmpty()) {
            WaitForContainer.waitForCondition(lineListener, builder.waitForSeconds());
        }
        if (builder.waitForMessage()!=null) {
            WaitForContainer.waitForCondition(new LogChecker(this, builder.waitForMessage()), builder.waitForSeconds());
        }
        if (!builder.waitForTcpPort().isEmpty()) {
            List<Integer> internalPorts = builder.waitForTcpPort();
            List<Integer> externalPorts = new ArrayList<>();
            for (Integer internalPort : internalPorts) {
                externalPorts.add(findExternalPort(internalPort));
            }
            WaitForContainer.waitForCondition(new TcpPortChecker(getDockerHost(), externalPorts), builder.waitForSeconds());
        }
        if (!builder.waitForHttpPing().isEmpty()) {
            for (Integer internalHttpPort : builder.waitForHttpPing()) {
                String pingUrl = String.format("http://%s:%s/", getDockerHost(), findExternalPort(internalHttpPort));
                WaitForContainer.waitForCondition(new HttpPingChecker(pingUrl), builder.waitForSeconds());
            }
        }
    }

    private Integer findExternalPort(Integer internalHttpPort) {
        String portAndProtocol = ExposePortBindingBuilder.containerBindWithProtocol(Integer.toString(internalHttpPort));
        if (! (containerPorts.containsKey(portAndProtocol)
                && containerPorts.get(portAndProtocol)!=null
                && containerPorts.get(portAndProtocol).size()>0)) {
            throw new PortNotExposedException(String.format("Port %s is not exposed and cannot be checked (exposed port info: %s)", portAndProtocol, containerPorts));
        }
        List<PortBinding> portBindings = containerPorts.get(portAndProtocol);
        PortBinding portBinding = portBindings.get(0);
        try {
            return Integer.parseInt(portBinding.hostPort());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Interal rule problem - unable to parse exposed port number", e);
        }
    }

    private void attachLogs(DockerClient dockerClient, String containerId, LineListener lineListener) throws IOException, InterruptedException {
        dockerLogs = new DockerLogs(dockerClient, containerId, lineListener);
        if (builder.stdoutWriter()!=null) {
            dockerLogs.setStdoutWriter(builder.stdoutWriter());
        }
        if (builder.stderrWriter()!=null) {
            dockerLogs.setStderrWriter(builder.stderrWriter());
        }
        dockerLogs.start();
    }

    private boolean imageAvaliable(DockerClient dockerClient, String imageName) throws DockerException, InterruptedException {
        String imageNameWithTag = imageNameWithTag(imageName);
        List<Image> listImages = dockerClient.listImages(ListImagesParam.danglingImages(false));
        for (Image image : listImages) {
            if (image.repoTags() != null && image.repoTags().contains(imageNameWithTag)) {
                log.debug("image '{}' found", imageNameWithTag);
                return true;
            }
        }
        log.debug("image '{}' not found", imageNameWithTag);
        return false;
    }

    private String imageNameWithTag(String imageName) {
        if (! StringUtils.contains(imageName, ':')) {
            return imageName + ":latest";
        } else {
            return imageName;
        }
    }

    /**
     * Stop and remove container.<br/>
     * This is {@link ExternalResource#before()} made available as public - it may be helpful in scenarios
     * when you want to use {@link DockerRule} and operate it manually.
     */
    @Override
    public final void after() {
        log.debug("after {}", containerShortId);
        try {
            dockerLogs.close();
            ContainerState state = dockerClient.inspectContainer(container.id()).state();
            log.debug("{} state {}", containerShortId, state);
            if (state.running()) {
                if (builder.stopOptions().contains(StopOption.KILL)) {
                    dockerClient.killContainer(container.id());
                    log.info("{} killed", containerShortId);
                } else {
                    dockerClient.stopContainer(container.id(), STOP_TIMEOUT);
                    log.info("{} stopped", containerShortId);
                }
            }
            if (builder.stopOptions().contains(StopOption.REMOVE)) {
                dockerClient.removeContainer(container.id(), DockerClient.RemoveContainerParam.removeVolumes());
                log.info("{} deleted", containerShortId);
                container = null;
            }
        } catch (DockerException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Address of docker host. <b>Please note this is address of docker host as seen by docker client library
     * so it may not be valid docker host address in different contexts</b>.
     * <br/>
     * For example, if tests are run in unix-like environment with docker host on the same machine,
     * it will contain 'localhost' and will not point to docker host from inside container.
     * In such cases one should use {@link #getDockerContainerGateway()}.
     */
    public final String getDockerHost() {
        return dockerClient.getHost();
    }

    /**
     * Address of docker container gateway.
     */
    public final String getDockerContainerGateway() {
        return containerGateway;
    }

    /**
     * Address of docker container.
     */
    public String getContainerIp() {
        return containerIp;
    }

    /**
     * Get host dynamic port given container port was mapped to.
     *
     * @param containerPort Container port. Typically it matches Dockerfile EXPOSE directive.
     * @return Host port container port is exposed on.
     */
    public final String getExposedContainerPort(String containerPort) {
        String key = containerPort + "/tcp";
        List<PortBinding> list = containerPorts.get(key);
        if (list == null || list.size() == 0) {
            throw new IllegalStateException(String.format("%s is not exposed", key));
        }
        if (list.size() == 0) {
            throw new IllegalStateException(String.format("binding list for %s is empty", key));
        }
        String firstExposedPort = list.get(0).hostPort();
        if (list.size() > 1) {
            log.warn("{} port {} is bound to multiple external ports, returning first one: {}", containerShortId, containerPort, firstExposedPort);
        }
        return firstExposedPort;
    }

    private void logNetworkSettings() {
        log.info("{} docker host: {}, ip: {}, gateway: {}, exposed ports: {}", containerShortId, dockerClient.getHost(), containerIp, containerGateway, containerPorts);
    }

    /**
     * Stop and wait till given string will show in container output.
     *
     * @param searchString String to wait for in container output.
     * @param waitTime Wait time.
     * @throws TimeoutException On wait timeout.
     *
     * @deprecated Use {@link #waitForLogMessage(String, int)} instead.
     */
    public void waitFor(final String searchString, int waitTime) throws TimeoutException, InterruptedException {
        waitForLogMessage(searchString, waitTime);
    }

    /**
     * Stop and wait till given string will show in container output.
     *
     * @param logSearchString String to wait for in container output.
     * @param waitTime Wait time.
     * @throws TimeoutException On wait timeout.
     */
    public void waitForLogMessage(final String logSearchString, int waitTime) throws TimeoutException, InterruptedException {
        WaitForContainer.waitForCondition(new LogChecker(this, logSearchString), waitTime);
    }

    /**
     * Block until container exit.
     */
    public void waitForExit() throws InterruptedException {
        try {
            dockerClient.waitContainer(container.id());
        } catch (DockerException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Container log.
     */
    public String getLog() {
        try (LogStream stream = dockerClient.logs(container.id(), LogsParam.stdout(), LogsParam.stderr());) {
            String fullLog = stream.readFully();
            if (log.isTraceEnabled()) {
                log.trace("{} full log: {}", containerShortId, StringUtils.replace(fullLog, "\n", "|"));
            }
            return fullLog;
        } catch (DockerException | InterruptedException e) {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Id of container (null if it is not yet been created or has been stopped).
     */
    public String getContainerId() {
        return (container!=null ? container.id() : null);
    }

    /**
     * Underlying library @{link ContainerInfo} data structure returned by {@link DockerClient#inspectContainer(String)} at container start.
     *
     * @return Started container info or <code>null</code> if container was not yet started.
     */
    public ContainerInfo getContainerInfo() {
        return containerInfo;
    }

    /**
     * {@link DockerClient} for direct container manipulation.
     */
    DockerClient getDockerClient() {
        return dockerClient;
    }

}
