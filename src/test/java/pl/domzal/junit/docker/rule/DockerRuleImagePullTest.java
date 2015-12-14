package pl.domzal.junit.docker.rule;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.ImageNotFoundException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.Image;

public class DockerRuleImagePullTest {

    private static Logger log = LoggerFactory.getLogger(DockerRule.class);

    @Rule
    public DockerRule helperRule = DockerRule.builder()//
            .imageName("busybox")//
            .build();

    private DockerClient helperClient = helperRule.getDockerClient();

    @Test(expected = ImagePullException.class)
    public void shouldFailOnNonExistingImage() {
        DockerRule.builder().imageName("nonexistingimage").build();
    }

    @Test
    public void shouldPullImage() throws Throwable {
        removeContainers(helperClient, "hello-world:latest");
        removeImage(helperClient, "hello-world:latest");

        DockerRule.builder().imageName("hello-world:latest").build();
        assertTrue(imageAvaliable(helperClient, "hello-world:latest"));
    }

    private void removeImage(DockerClient dockerClient, String imageName) throws DockerException, InterruptedException {
        try {
            dockerClient.removeImage(imageName);
        } catch (ImageNotFoundException e) {
            // remove if it exists, if it's not this is OK
        }
        assertFalse(imageAvaliable(dockerClient, imageName));
    }

    private void removeContainers(DockerClient dockerClient, String imageName) throws DockerException, InterruptedException {
        log.debug("about to cleanup all containers created from image {}", imageName);
        List<Container> containers = dockerClient.listContainers(ListContainersParam.allContainers());
        for (Container container : containers) {
            if (imageName.equals(container.image())) {
                ContainerInfo containerInfo = dockerClient.inspectContainer(container.id());
                if (containerInfo.state().running()) {
                    throw new IllegalStateException(String.format("container '%s' based on image '%s' is running, this test requires removing all containers created from image '%s'"));
                } else {
                    log.debug("removing container {}", container.id());
                    dockerClient.removeContainer(container.id());
                }
            }
        }
    }

    private boolean imageAvaliable(DockerClient dockerClient, String imageNameWithTag) throws DockerException, InterruptedException {
        List<Image> listImages = dockerClient.listImages(ListImagesParam.danglingImages(false));
        for (Image image : listImages) {
            if (image.repoTags().contains(imageNameWithTag)) {
                return true;
            }
        }
        return false;
    }

}
