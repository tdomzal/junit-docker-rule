package pl.domzal.junit.docker.rule.examples;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.domzal.junit.docker.rule.AssertHtml;
import pl.domzal.junit.docker.rule.DockerRule;

public class ExampleExposeContainerPortStaticTest {

    private static Logger log = LoggerFactory.getLogger(ExampleExposeContainerPortStaticTest.class);

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .imageName("nginx")//
            .publishAllPorts(false)//
            .expose("8123", "80")
            .build();

    @Test
    public void shouldExposeSpecifiedPort() throws InterruptedException, IOException {
        String nginxHome = "http://"+testee.getDockerHost()+":8123/";
        log.info("homepage: {}", nginxHome);
        assertTrue(AssertHtml.pageContainsString(nginxHome, "Welcome to nginx!"));
    }

}
