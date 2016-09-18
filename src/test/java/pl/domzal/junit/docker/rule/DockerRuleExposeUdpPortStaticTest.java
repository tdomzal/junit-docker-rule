package pl.domzal.junit.docker.rule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(test.category.Stable.class)
public class DockerRuleExposeUdpPortStaticTest {

    @Rule
    public DockerRule testee = DockerRule.builder()//
            .imageName("alpine")//
            .expose("4445", "4445/udp")//
            .cmd("sh", "-c", "echo started; nc -l -u -p 4445")
            .waitFor(WaitFor.logMessage("started"))
            .build();

    @Test
    public void shouldExposeSpecifiedUdpPort() throws Throwable {
        DockerRule sender = DockerRule.builder()//
                .imageName("alpine")//
                .extraHosts("serv:"+testee.getDockerContainerGateway())
                .cmd("sh", "-c", "echo 12345 | nc -u serv 4445")//
                .build();
        sender.before();
        try {
            testee.waitForLogMessage("12345", 5);
        } finally {
            sender.after();
        }
    }

}
