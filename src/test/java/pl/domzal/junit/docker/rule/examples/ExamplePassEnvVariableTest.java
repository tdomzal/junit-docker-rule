package pl.domzal.junit.docker.rule.examples;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.spotify.docker.client.exceptions.DockerException;

import pl.domzal.junit.docker.rule.DockerRule;

/**
 * Pass environment variable to container.
 */
@Category(test.category.Stable.class)
public class ExamplePassEnvVariableTest {

    @Rule
    public DockerRule testee = DockerRule.builder()
            .imageName("busybox:1.25.1")
            .env("EXTRA_OPT", "EXTRA_OPT_VALUE")
            .cmd("sh", "-c", "echo $EXTRA_OPT")
            .build();

    @Test
    public void shouldPassEnvVariables() throws InterruptedException, IOException, DockerException {
        testee.waitForExit();
        String output = testee.getLog();
        assertThat(output, containsString("EXTRA_OPT_VALUE"));
    }


}
