package pl.domzal.junit.docker.rule.examples;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.domzal.junit.docker.rule.AssertHtml;
import pl.domzal.junit.docker.rule.DockerRule;

/**
 * Wait for http service to be available.
 */
@Category(test.category.Stable.class)
public class ExampleWaitForHttpPingTest {

    private static Logger log = LoggerFactory.getLogger(ExampleWaitForHttpPingTest.class);

    @Rule
    public DockerRule httpd = DockerRule.builder()
            .imageName("nginx")
            .publishAllPorts(true)
            // wait until container port 80 will response to HTTP HEAD request
            .waitForHttpPing(80)
            // delayed httpd start ... to mimic long starting http server
            .cmd("sh", "-c", "echo waiting...; sleep 5; echo starting...; nginx -g 'daemon off;'")
            .build();

    @Test
    public void shouldWait() throws Throwable {
        String nginxHome = "http://" + httpd.getDockerHost() + ":"+httpd.getExposedContainerPort("80")+"/";
        log.info("homepage: {}", nginxHome);

        assertTrue(AssertHtml.pageContainsStringNoRetry(nginxHome, "Welcome to nginx!"));
    }

}
