package pl.domzal.junit.docker.rule.examples;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.domzal.junit.docker.rule.AssertHtml;
import pl.domzal.junit.docker.rule.DockerRule;

/**
 * Port 80 specified in Nginx Dockerfile is exposed to dynamically selected port on docker host.
 */
@Category(test.category.Stable.class)
public class ExamplePortExposeAllTest {

    private static Logger log = LoggerFactory.getLogger(ExamplePortExposeAllTest.class);

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .imageName("nginx")//
            .build();

    @Test
    public void shouldExposeAllPorts() throws InterruptedException, IOException {
        String nginxHome = "http://"+testee.getDockerHost()+":"+testee.getExposedContainerPort("80")+"/";
        log.info("homepage: {}", nginxHome);
        assertTrue(AssertHtml.pageContainsString(nginxHome, "Welcome to nginx!"));
    }

}
