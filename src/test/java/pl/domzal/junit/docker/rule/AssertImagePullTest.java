package pl.domzal.junit.docker.rule;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Image;

public class AssertImagePullTest {

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .setImageName("busybox")//
            .build();

    @Test
    public void checkListImages() throws InterruptedException, IOException, DockerException {
        DockerClient dockerClient = testee.getDockerClient();
        dockerClient.pull("busybox:latest");;
        assertFalse(imageAvaliable(dockerClient, "nonexistingimage:latest"));
        assertTrue(imageAvaliable(dockerClient, "busybox:latest"));
    }

    private boolean imageAvaliable(DockerClient dockerClient, String imageNameAndTag) throws DockerException, InterruptedException {
        List<Image> listImages = dockerClient.listImages(ListImagesParam.danglingImages(false));
        for (Image image : listImages) {
            if (image.repoTags().contains(imageNameAndTag)) {
                return true;
            }
        }
        return false;
    }

}
