package pl.domzal.junit.docker.rule;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.domzal.junit.docker.rule.ex.PortNotExposedException;

@Category(test.category.Unstable.class)
public class DockerRuleWaitForPortTest {

    private static Logger log = LoggerFactory.getLogger(DockerRuleWaitForPortTest.class);

    @Test(expected = PortNotExposedException.class)
    public void beforeShouldFailWhenPublishAllPortsIsFalse() throws Throwable {
        DockerRule httpd = DockerRule.builder() //
                .imageName("nginx")//
                .publishAllPorts(false)
                .waitForTcpPort(80)
                .build();
        try {
            httpd.before();
        } finally {
            httpd.after();
        }
    }

    @Test
    public void beforeShouldSuccessWhenPublishAllPortsIsTrue() throws Throwable {
        DockerRule httpd = DockerRule.builder() //
                .imageName("nginx")//
                .waitForTcpPort(80)
                .build();
        try {
            httpd.before();
        } finally {
            httpd.after();
        }
    }

    @Test
    public void beforeShouldSuccessWhenWaitHttpPortIsExposed() throws Throwable {
        DockerRule httpd = DockerRule.builder() //
                .imageName("nginx")//
                .expose("8124", "80")//
                .waitForTcpPort(80)
                .build();
        try {
            httpd.before();
        } finally {
            httpd.after();
        }
    }

    @Test
    public void shouldFailWithoutWaitPort() throws Throwable {
        DockerRule httpd = DockerRule.builder() //
                .imageName("nginx")//
                .expose("8124", "80")//
                .cmd("sh", "-c", "echo waiting...; sleep 5; echo starting...; nginx -g 'daemon off;'")
                .build();

        try {
            httpd.before();
            String nginxHome = "http://" + httpd.getDockerHost() + ":8124/";
            log.info("homepage: {}", nginxHome);

            assertTrue(AssertHtml.pageContainsString(nginxHome, "Welcome to nginx!", 1, 0));

            fail("expected to fail when rule is not waiting to httpd to be up and running but, server appears to be running");
        } catch (IllegalStateException e) {
            // expected pageContains failure
        } finally {
            httpd.after();
        }
    }

    @Test
    public void shouldWaitForPort() throws Throwable {
        DockerRule httpd = DockerRule.builder() //
                .imageName("nginx")//
                .expose("8124", "80")//
                .waitForTcpPort(80)
                .cmd("sh", "-c", "echo waiting...; sleep 5; echo starting...; nginx -g 'daemon off;'")
                .build();

        try {
            httpd.before();
            String nginxHome = "http://" + httpd.getDockerHost() + ":8124/";
            log.info("homepage: {}", nginxHome);

            assertTrue(AssertHtml.pageContainsString(nginxHome, "Welcome to nginx!", 1, 0));
        } catch (IllegalStateException e) {
            fail("httpd is not available so it looks like wait for http ping is not working in rule, exception message is: "+e.getMessage());
        } finally {
            httpd.after();
        }
    }


}
