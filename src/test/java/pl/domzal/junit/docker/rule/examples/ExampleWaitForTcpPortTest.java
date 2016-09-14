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
 * Wait for open TCP server port.
 * Plase read {@link pl.domzal.junit.docker.rule.DockerRuleBuilder#waitForTcpPort(int...)} notes.
 */
@Category(test.category.Unstable.class)
public class ExampleWaitForTcpPortTest {

    private static Logger log = LoggerFactory.getLogger(ExampleWaitForTcpPortTest.class);

    @Rule
    public DockerRule httpd = DockerRule.builder()
            .imageName("nginx")
            .publishAllPorts(true)
            // port we are waiting for
            .waitForTcpPort(80)
            // delayed httpd start ...
            .cmd("sh", "-c", "echo waiting...; sleep 5; echo starting...; nginx -g 'daemon off;'")
            .build();

    @Test
    public void shouldWait() throws Throwable {
        String nginxHome = "http://" + httpd.getDockerHost() + ":"+httpd.getExposedContainerPort("80")+"/";
        log.info("homepage: {}", nginxHome);

        assertTrue(AssertHtml.pageContainsStringNoRetry(nginxHome, "Welcome to nginx!"));
    }

}
