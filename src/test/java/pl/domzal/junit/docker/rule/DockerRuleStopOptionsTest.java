package pl.domzal.junit.docker.rule;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.DockerRequestException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;

@Category(test.category.Stable.class)
public class DockerRuleStopOptionsTest {

    @Test
    public void shouldKeepContainer() throws Throwable {

        DockerRule testee = DockerRule.builder()//
                .imageName("alpine")//
                .cmd("sh", "-c", "sleep 1")//
                .stopOptions(StopOption.KEEP)
                .build();

        final DockerClient dockerClient = testee.getDockerClient();
        testee.before();
        final String containerId = testee.getContainerId();

        try {
            testee.after();
            assertContainerExists(dockerClient, containerId);
        } finally {
            cleanup(dockerClient, containerId);
        }
    }

    @Test
    public void shouldStopContainer() throws Throwable {
        DockerRule testee = DockerRule.builder()//
                .imageName("alpine")//
                .cmd("sh", "-c", "trapinfo() { echo STOPPED; exit; }; trap \"trapinfo\" SIGTERM; for i in 01 02 03 04 05 06 07 08 09 10; do (echo $i; sleep 1); done")//
                .stopOptions(StopOption.KEEP, StopOption.STOP)
                .build();

        final DockerClient dockerClient = testee.getDockerClient();
        testee.before();
        final String containerId = testee.getContainerId();

        try {
            testee.after();
            assertTrue(StringUtils.trimToEmpty(testee.getLog()).contains("STOPPED"));
        } finally {
            cleanup(dockerClient, containerId);
        }
    }

    @Test
    public void shouldKillContainer() throws Throwable {
        DockerRule testee = DockerRule.builder()//
                .imageName("alpine")//
                .cmd("sh", "-c", "trapinfo() { echo STOPPED; exit; }; trap \"trapinfo\" SIGTERM; for i in 01 02 03 04 05 06 07 08 09 10; do (echo $i; sleep 1); done")//
                .stopOptions(StopOption.KEEP, StopOption.KILL)
                .build();

        final DockerClient dockerClient = testee.getDockerClient();
        testee.before();
        final String containerId = testee.getContainerId();

        try {
            testee.after();
            assertFalse(StringUtils.trimToEmpty(testee.getLog()).contains("STOPPED"));
        } finally {
            cleanup(dockerClient, containerId);
        }

    }

    private void assertContainerExists(DockerClient dockerClient, String containerId) throws DockerException, InterruptedException {
        assertTrue(String.format("container %s should exist", containerId), containerExists(dockerClient, containerId));
    }

    private void cleanup(DockerClient dockerClient, String containerId) throws DockerException, InterruptedException {
        try {
            if (containerExists(dockerClient, containerId)) {
                ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);
                if (containerInfo.state().running()) {
                    dockerClient.killContainer(containerId);
                }
                dockerClient.removeContainer(containerId, true);
            }
        } catch (DockerRequestException dockerRequestException) {
            throw new IllegalStateException(dockerRequestException.message(), dockerRequestException);
        }
    }

    private boolean containerExists(DockerClient dockerClient, String containerId) throws DockerException, InterruptedException {
        return containersContains(dockerClient.listContainers(
                DockerClient.ListContainersParam.allContainers(true)), containerId);
    }

    private boolean containersContains(List<Container> containers, String containerId) {
        for (Container container : containers) {
            if (containerId.equals(container.id())) {
                return true;
            }
        }
        return false;
    }

}
