package pl.domzal.junit.docker.rule.examples;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.domzal.junit.docker.rule.DockerRule;
import pl.domzal.junit.docker.rule.wait.StartCondition;
import pl.domzal.junit.docker.rule.wait.StartConditionCheck;

/**
 * Simple custom 'wait for message in log' startup condition (just as example - similar
 * condition is already available in {@link pl.domzal.junit.docker.rule.WaitFor}).
 */
@Category(test.category.Stable.class)
public class ExampleWaitForCustomTest {

    @Rule
    public DockerRule testee = DockerRule.builder()//
            .imageName("busybox:1.25.1")//
            .cmd("sh", "-c", "for i in 01 02 started 03 04 05; do (echo $i; sleep 1); done")//
            .waitFor(new WaitForMessage())
            .build();

    @Test
    public void shouldWaitForLogMessage() throws Throwable {
        String log = testee.getLog();
        assertThat(log, containsString("02"));
        assertThat(log, not(containsString("05")));
    }

    private static class WaitForMessage implements StartCondition {

        public static final String WAIT_FOR_MESSAGE = "started";

        @Override
        public StartConditionCheck build(final DockerRule currentRule) {
            return new StartConditionCheck() {
                @Override
                public boolean check() {
                    return currentRule.getLog().contains(WAIT_FOR_MESSAGE);
                }

                @Override
                public String describe() {
                    return String.format("'%s' in log file", WAIT_FOR_MESSAGE);
                }

                @Override
                public void after() {}
            };
        }
    }
}
