package pl.domzal.junit.docker.rule.examples;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.domzal.junit.docker.rule.AssertHtml;
import pl.domzal.junit.docker.rule.DockerRule;
import pl.domzal.junit.docker.rule.WaitFor;

/**
 * Wait for open TCP server port.
 * <b>Please note it may not work with default docker daemon settings on many platforms</b>
 * - see {@link WaitFor#tcpPort(int...)} notes for more info.
 */
@Category(test.category.TcpPorts.class)
public class ExampleWaitForTcpPortTest {

    private static Logger log = LoggerFactory.getLogger(ExampleWaitForTcpPortTest.class);

    @Rule
    public DockerRule httpd = DockerRule.builder()
            .imageName("nginx")
            .publishAllPorts(true)
            // port we are waiting for
            .waitFor(WaitFor.tcpPort(80))
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
