package pl.domzal.junit.docker.rule.examples;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.domzal.junit.docker.rule.DockerRule;
import pl.domzal.junit.docker.rule.WaitFor;

/**
 * Is it possible to wait for specified message in container log.
 */
@Category(test.category.Stable.class)
public class ExampleWaitForLogMessageAtStartTest {

    @Rule
    public DockerRule testee = DockerRule.builder()//
            .imageName("busybox")//
            .cmd("sh", "-c", "for i in 01 02 started 03 04 05; do (echo $i; sleep 1); done")//
            .waitFor(WaitFor.logMessage("started"))
            .build();

    @Test
    public void shouldWaitForLogMessage() throws Throwable {
        String log = testee.getLog();
        assertThat(log, containsString("02"));
        assertThat(log, not(containsString("05")));
    }

}
