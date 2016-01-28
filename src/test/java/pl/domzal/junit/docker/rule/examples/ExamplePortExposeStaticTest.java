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
 * Is it possible to expose port manually.
 */
@Category(test.category.Stable.class)
public class ExamplePortExposeStaticTest {

    private static Logger log = LoggerFactory.getLogger(ExamplePortExposeStaticTest.class);

    @ClassRule
    public static DockerRule testee = DockerRule.builder() //
            .imageName("nginx") //
            .publishAllPorts(false) // publishAllPorts is disabled when expose(...) is used but we make it explicit here
            .expose("8123", "80")
            .build();

    @Test
    public void shouldExposeSpecifiedPort() throws InterruptedException, IOException {
        String nginxHome = "http://"+testee.getDockerHost()+":8123/";
        log.info("homepage: {}", nginxHome);
        assertTrue(AssertHtml.pageContainsString(nginxHome, "Welcome to nginx!"));
    }

}
