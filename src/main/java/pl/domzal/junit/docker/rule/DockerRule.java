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
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.ImageNotFoundException;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
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

    private static final int STOP_TIMEOUT = 5;

    private final DockerClient dockerClient;
    private ContainerCreation container;

    private final DockerRuleBuiler builder;
    private Map<String, List<PortBinding>> containerPorts;

    public DockerRule(DockerRuleBuiler builder) {

        this.builder = builder;

        HostConfig hostConfig = HostConfig.builder()//
                .portBindings(hostPortBindings(builder.getExposedPorts()))//
                .extraHosts(builder.getExtraHosts())//
                .build();

        ContainerConfig containerConfig = ContainerConfig.builder()//
                .hostConfig(hostConfig)//
                .image(builder.getImageName())//
                .networkDisabled(false)//
                //.hostname("bleble:127.0.0.1")
                .cmd(builder.getCmd()).build();

        try {

            dockerClient = DefaultDockerClient.fromEnv().build();

            if (builder.getImageAlwaysPull() || ! imageAvaliable(dockerClient, builder.getImageName())) {
                dockerClient.pull(builder.getImageName());
            }

            container = dockerClient.createContainer(containerConfig);

            log.info("container {} started, id {}", builder.getImageName(), container.id());
        } catch (ImageNotFoundException e) {
            throw new ImagePullException(String.format("Image '%s' not found", builder.getImageName()), e);
        } catch (DockerException | InterruptedException | DockerCertificateException e) {
            throw new IllegalStateException(e);
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
        if (builder.getWaitForMessage()!=null) {
            waitForMessage();
        }
        logMappings(dockerClient);
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
        final String waitForMessage = builder.getWaitForMessage();
        log.info("{} waiting for log message '{}'", container.id(), waitForMessage);
        new WaitForUnit(TimeUnit.SECONDS, 30, new WaitForCondition(){
            @Override
            public boolean isConditionMet() {
                return fullLogContent().contains(waitForMessage);
            }
            @Override
            public String timeoutMessage() {
                return String.format("Timeout waiting for '%s'", waitForMessage);
            }
        }).startWaiting();
        log.debug("{} message '{}' found", container.id(), waitForMessage);
    }

    @Override
    protected void after() {
        super.after();
        try {
            ContainerState state = dockerClient.inspectContainer(container.id()).state();
            log.debug("container state: {}", state);
            if (state.running()) {
                dockerClient.stopContainer(container.id(), STOP_TIMEOUT);
                log.info("{} stopped", container.id());
            }
            if (!builder.getKeepContainer()) {
                dockerClient.removeContainer(container.id(), true);
                log.info("{} deleted", container.id());
            }
        } catch (DockerException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public final String getDockerHost() {
        return dockerClient.getHost();
    }

    /**
     * Get host port conteiner internal port was mapped to.
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

    private void logMappings(DockerClient dockerClient) throws DockerException, InterruptedException {
        ContainerInfo inspectContainer = dockerClient.inspectContainer(container.id());
        NetworkSettings networkSettings = inspectContainer.networkSettings();
        log.info("{} exposed ports: {}", container.id(), networkSettings.ports());
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
