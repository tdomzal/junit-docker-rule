package pl.domzal.junit.docker.rule;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;

@Category(test.category.Stable.class)
public class DockerRuleWaitForMessageSequenceTest {

    private static Logger log = LoggerFactory.getLogger(DockerRuleWaitForMessageSequenceTest.class);

    @Rule
    public DockerRule testee = DockerRule.builder()//
            .imageName("busybox")//
            .cmd("sh", "-c", "for i in 01 TICK 02 03 04 05 TICK 06 07 08 09 10; do (echo $i; sleep 1); done")//
            .waitForMessageSequence("TICK")//
            .nextMessage("TICK")//
            .waitDone()//
            .build();

    @Test
    public void shouldWaitForLogMessage() throws InterruptedException, TimeoutException, DockerException {
        String stdout = testee.getLog();
        log.info("log:\n{}", stdout);
        assertThat("should not stop before first TICK", stdout, containsString("TICK"));
        assertThat("should stop on second TICK so log should contain 05", stdout, containsString("05"));
        assertThat("should not wait for container stop so log should not contain 10", stdout, not(containsString("10")));
    }

    @After
    public void closeContainer() throws DockerException, InterruptedException {
        DockerClient dockerClient = testee.getDockerClient();
        dockerClient.waitContainer(testee.getContainerId());
    }

}
