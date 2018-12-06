package pl.domzal.junit.docker.rule;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.HostConfig;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

@Category(test.category.Stable.class)
public class DockerRuleExtraUlimitsTest {

    private static Logger log = LoggerFactory.getLogger(DockerRuleExtraUlimitsTest.class);

    @Rule
    public DockerRule testee = DockerRule.builder()//
            .imageName("busybox:1.25.1")//
            .ulimit(HostConfig.Ulimit.create("nofile", (long)262144,(long)262144 ))
            .cmd("sh", "-c", "ulimit -a | grep descriptors")//
            .build();

    @Test
    public void shouldDefineUlimits() throws InterruptedException, IOException, DockerException {

        DockerClient dockerClient = testee.getDockerClient();
        dockerClient.waitContainer(testee.getContainerId());
        log.info("done");

        LogStream stdoutLog = dockerClient.logs(testee.getContainerId(), LogsParam.stdout(), LogsParam.stderr());
        String stdout = StringUtils.trim(stdoutLog.readFully());
        log.info("log:\n{}", stdout);
        assertThat(stdout, containsString("262144"));

    }
}
