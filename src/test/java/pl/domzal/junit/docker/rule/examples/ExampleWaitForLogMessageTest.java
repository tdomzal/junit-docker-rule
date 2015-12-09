package pl.domzal.junit.docker.rule.examples;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import pl.domzal.junit.docker.rule.DockerRule;

public class ExampleWaitForLogMessageTest {

    @Rule
    public DockerRule testee = DockerRule.builder()//
            .imageName("busybox")//
            .cmd("sh", "-c", "for i in 01 02 started 03 04 05; do (echo $i; sleep 1); done")//
            .waitForMessage("started")
            .build();

    @Test
    public void shouldReadMountFromJavaFile() throws Throwable {
        String log = testee.getLog();
        assertThat(log, not(containsString("05")));
        testee.waitFor("05", 10);
    }

}
