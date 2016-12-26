package pl.domzal.junit.docker.rule.examples;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.domzal.junit.docker.rule.DockerRule;
import pl.domzal.junit.docker.rule.WaitFor;

/**
 * Start and stop container from test case (use {@link DockerRule} to build container but use it manually).
 */
@Category(test.category.Stable.class)
public class ExampleDockerContainterTest {

    @Test
    public void shouldStartAndStopContainerTwice() throws Throwable {

        DockerRule testee = DockerRule.builder()
                .imageName("busybox:1.25.1")
                .cmd("sh", "-c", "for i in 01 02 started 03 04 05; do (echo $i; sleep 1); done")
                .waitFor(WaitFor.logMessage("started"))
                .build();

        testee.before();
        try {
            String log = testee.getLog();
            assertThat(log, not(containsString("05")));
        } finally {
            testee.after();
        }

        testee.before();
        try {

            String log = testee.getLog();
            assertThat(log, not(containsString("05")));
        } finally {
            testee.after();
        }

    }

}
