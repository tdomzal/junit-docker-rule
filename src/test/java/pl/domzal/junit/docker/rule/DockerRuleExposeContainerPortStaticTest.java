package pl.domzal.junit.docker.rule;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerRuleExposeContainerPortStaticTest {

    private static Logger log = LoggerFactory.getLogger(DockerRuleExposeContainerPortStaticTest.class);

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .imageName("nginx")//
            .expose("8123", "80")//
            .build();

    private String nginxHome;

    @Before
    public void setupHomepage() {
        nginxHome = "http://"+testee.getDockerHost()+":8123/";
        log.info("homepage: {}", nginxHome);
    }

    @Test
    public void shouldExposeSpecifiedPort() throws InterruptedException, IOException {
        Thread.sleep(1000);
        assertTrue(AssertHtml.pageContainsString(nginxHome, "Welcome to nginx!"));
    }

}
