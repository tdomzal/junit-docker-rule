package pl.domzal.junit.docker.rule;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;

public class DockerRuleExposeContainerPortBlocksPublishingTest {

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .imageName("nginx")//
            .expose("8123", "81")//
            .build();

    @Test(expected = IllegalStateException.class)
    public void shouldRaiseExceptionOnGetUnexposedPort() throws InterruptedException, IOException {
        testee.getExposedContainerPort("80");
    }

}
