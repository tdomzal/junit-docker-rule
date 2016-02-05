package pl.domzal.junit.docker.rule;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.DockerRequestException;
import com.spotify.docker.client.ImageNotFoundException;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.PortBinding;

import pl.domzal.junit.docker.rule.WaitForUnit.WaitForCondition;

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
 * Inspired by and loosely based on osheeshel/DockerContainerRule - see {@link https://gist.github.com/mosheeshel/c427b43c36b256731a0b}.
 */
public class DockerRule extends ExternalResource {

    private static Logger log = LoggerFactory.getLogger(DockerRule.class);

    private static final int STOP_TIMEOUT = 5;

    private final DockerClient dockerClient;
    private ContainerCreation container;

    private final DockerRuleBuilder builder;
    private final String imageNameWithTag;
    private String containerIp;
    private String containerGateway;
    private Map<String, List<PortBinding>> containerPorts;

    private DockerLogs dockerLogs;


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
                .extraHosts(builder.extraHosts())//
                .build();
        ContainerConfig containerConfig = ContainerConfig.builder()//
                .hostConfig(hostConfig)//
                .image(imageNameWithTag)//
                .env(builder.env())//
                .networkDisabled(false)//
                .exposedPorts(builder.containerExposedPorts())
                .entrypoint(builder.entrypoint())
                .cmd(builder.cmd()).build();
        try {
            this.container = dockerClient.createContainer(containerConfig);
            log.debug("rule before {}", container.id());
            log.info("container {} created, id {}", imageNameWithTag, container.id());

            dockerClient.startContainer(container.id());
            log.debug("{} started", container.id());

            attachLogs(dockerClient, container.id());

            ContainerInfo containerInfo = dockerClient.inspectContainer(container.id());
            containerIp = containerInfo.networkSettings().ipAddress();
            containerPorts = containerInfo.networkSettings().ports();
            containerGateway = containerInfo.networkSettings().gateway();
            if (builder.waitForMessage()!=null) {
                waitForMessage();
            }
            logNetworkSettings();

        } catch (DockerRequestException e) {
            throw new IllegalStateException(e.message(), e);
        } catch (DockerException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void attachLogs(DockerClient dockerClient, String containerId) throws IOException, InterruptedException {
        dockerLogs = new DockerLogs(dockerClient, containerId);
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
            if (image.repoTags().contains(imageNameWithTag)) {
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

    private void waitForMessage() throws TimeoutException, InterruptedException {
        final String waitForMessage = builder.waitForMessage();
        log.info("{} waiting for log message '{}'", container.id(), waitForMessage);
        new WaitForUnit(TimeUnit.SECONDS, builder.waitForMessageSeconds(), new WaitForCondition(){
            @Override
            public boolean isConditionMet() {
                return getLog().contains(waitForMessage);
            }
            @Override
            public String timeoutMessage() {
                return String.format("Timeout waiting for '%s'", waitForMessage);
            }
        }).startWaiting();
        log.debug("{} message '{}' found", container.id(), waitForMessage);
    }

    /**
     * Stop and remove container.<br/>
     * This is {@link ExternalResource#before()} made available as public - it may be helpful in scenarios
     * when you want to use {@link DockerRule} and operate it manually.
     */
    @Override
    public final void after() {
        log.debug("after {}", container.id());
        try {
            dockerLogs.close();
            ContainerState state = dockerClient.inspectContainer(container.id()).state();
            log.debug("{} state {}", container.id(), state);
            if (state.running()) {
                dockerClient.stopContainer(container.id(), STOP_TIMEOUT);
                log.info("{} stopped", container.id());
            }
            if (!builder.keepContainer()) {
                dockerClient.removeContainer(container.id(), true);
                log.info("{} deleted", container.id());
                container = null;
            }
        } catch (DockerException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
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
     * Get host dynamic port given container port was mapped to.
     *
     * @param containerPort Container port. Typically it matches Dockerfile EXPOSE directive.
     * @return Host port conteiner port is exposed on.
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

        if (list.size() > 1) {
            throw new IllegalStateException(String.format("binding list for %s is longer than 1", key));
        }
        return list.get(0).hostPort();
    }

    private void logNetworkSettings() {
        log.info("{} docker host: {}, ip: {}, gateway: {}, exposed ports: {}", container.id(), dockerClient.getHost(), containerIp, containerGateway, containerPorts);
    }

    /**
     * Stop and wait till given string will show in container output.
     *
     * @param searchString String to wait for in container output.
     * @param waitTime Wait time.
     * @throws TimeoutException On wait timeout.
     */
    public void waitFor(final String searchString, int waitTime) throws TimeoutException, InterruptedException {
        new WaitForUnit(TimeUnit.SECONDS, waitTime, TimeUnit.SECONDS, 1, new WaitForCondition() {
            @Override
            public boolean isConditionMet() {
                return StringUtils.contains(getLog(), searchString);
            }

            @Override
            public String tickMessage() {
                return String.format("wait for '%s' in log", searchString);
            }

            @Override
            public String timeoutMessage() {
                return String.format("container log: \n%s", getLog());
            }

        }) //
        .startWaiting();
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
                log.trace("{} full log: {}", container.id(), StringUtils.replace(fullLog, "\n", "|"));
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
     * {@link DockerClient} for direct container manipulation.
     */
    DockerClient getDockerClient() {
        return dockerClient;
    }

}
