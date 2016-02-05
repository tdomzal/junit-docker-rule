package pl.domzal.junit.docker.rule;

import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.NetworkSettings;

public class DockerRuleExposePortNotExposedTcpTest {

    private static Logger log = LoggerFactory.getLogger(DockerRuleExposePortNotExposedTcpTest.class);

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .imageName("alpine")//
            .expose("4444", "4444")//
            .cmd("sh", "-c", "echo started; nc -l -p 4444")
            .waitForMessage("started")
            .build();

    @Test
    public void shouldExposeSpecifiedPort() throws Throwable {
        ContainerInfo containerInfo = testee.getDockerClient().inspectContainer(testee.getContainerId());
        NetworkSettings networkSettings = containerInfo.networkSettings();
        String gateway = networkSettings.gateway();
        String hostAddress = testee.getDockerHost();

        log.info("client.getHost() = {}, client.network.gateway() = {}", hostAddress, gateway);

        DockerRule sender = DockerRule.builder()//
                .imageName("alpine")//
                .cmd("sh", "-c", "echo 12345 | nc "+testee.getDockerContainerGateway()+" 4444; echo done")//
                .waitForMessage("done")
                .build();
        sender.before();
        try {
            testee.waitFor("12345", 5);
        } finally {
            sender.after();
        }
    }

}
