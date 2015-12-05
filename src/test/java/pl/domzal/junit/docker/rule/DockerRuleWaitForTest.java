package pl.domzal.junit.docker.rule;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParam;

import pl.domzal.junit.docker.rule.DockerRule;

import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.LogStream;

public class DockerRuleWaitForTest {

    private static Logger log = LoggerFactory.getLogger(DockerRuleWaitForTest.class);

    @Rule
    public DockerRule testee = DockerRule.builder()//
            .imageName("busybox")//
            .cmd("sh", "-c", "for i in 01 02 03 04 05 06 07 08 09 10; do (echo $i; sleep 1); done")//
            .waitForMessage("05")
            .build();

    @Test
    public void shouldWaitForLogMessage() throws InterruptedException, TimeoutException, DockerException {

        DockerClient dockerClient = testee.getDockerClient();

        LogStream stdoutLog = dockerClient.logs(testee.getContainerId(), LogsParam.stdout(), LogsParam.stderr());
        String stdout = StringUtils.trim(stdoutLog.readFully());
        log.info("log:\n{}", stdout);
        assertThat("should wait for 05", stdout, containsString("05"));
        assertThat("should stop waiting after 05", stdout, not(containsString("10")));
    }

    @After
    public void closeContainer() throws DockerException, InterruptedException {
        DockerClient dockerClient = testee.getDockerClient();
        dockerClient.waitContainer(testee.getContainerId());
    }

}
