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
 * Is it possible to expose single port to randomly allocated host port.
 */
@Category(test.category.Stable.class)
public class ExamplePortExposeDynamicTest {

    private static Logger log = LoggerFactory.getLogger(ExamplePortExposeDynamicTest.class);

    @ClassRule
    public static DockerRule testee = DockerRule.builder() //
            .imageName("nginx") //
            .expose("80") // specify only container port to expose to random host port
            .build();

    @Test
    public void shouldExposeSpecifiedPort() throws InterruptedException, IOException {
        String nginxHome = "http://"+testee.getDockerHost()+":"+testee.getExposedContainerPort("80")+"/";
        log.info("homepage: {}", nginxHome);
        assertTrue(AssertHtml.pageContainsString(nginxHome, "Welcome to nginx!"));
    }

}
