package pl.domzal.junit.docker.rule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.DockerException;

public class DockerRuleExposeTcpPortStaticTest {

    private static final Logger log = LoggerFactory.getLogger(DockerRuleExposeTcpPortStaticTest.class);

    @Rule
    public DockerRule testee = DockerRule.builder() //
            .imageName("alpine") //
            .expose("4444", "4444") //
            .cmd("sh", "-c", "echo started; nc -l -p 4444") //
            .waitForMessage("started") //
            .build();

    @Before
    public void logNetworkConfig() throws DockerException, InterruptedException {
        log.debug("server.network: {}", testee.getDockerClient().inspectContainer(testee.getContainerId()).networkSettings());
    }

    @Test
    public void shouldExposeSpecifiedPort() throws Throwable {
        String testeeIp = testee.getDockerContainerIp();
        log.debug("server.ip: {}", testeeIp);
        DockerRule sender = DockerRule.builder() //
                .imageName("alpine") //
                .extraHosts("serv:"+testeeIp)
                .cmd("sh", "-c", "ping -w 3 $(ip route | grep default | cut -d ' ' -f 3) | echo 12345 | nc serv 4444; echo done") //
                .waitForMessage("done") //
                .build();
        sender.before();
        try {
            log.debug("sender.network: {}", sender.getDockerClient().inspectContainer(sender.getContainerId()).networkSettings());
            testee.waitFor("12345", 5);
        } finally {
            sender.after();
        }
    }

}
