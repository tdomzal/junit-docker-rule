package pl.domzal.junit.docker.rule;

import org.junit.ClassRule;
import org.junit.Test;

public class DockerRuleExposePortNotExposedTcpTest {

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .imageName("alpine")//
            .expose("4444", "4444")//
            .cmd("sh", "-c", "echo started; nc -l -p 4444")
            .waitForMessage("started")
            .build();

    @Test
    public void shouldExposeSpecifiedPort() throws Throwable {
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
