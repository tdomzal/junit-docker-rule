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
 * Add extra host entry to container <code>/etc/hosts</code>.
 */
@Category(test.category.Stable.class)
public class ExampleAddExtraHostTest {

    @Rule
    public DockerRule testee = DockerRule.builder()
            .imageName("busybox:1.25.1")
            .extraHosts("extrahost:1.2.3.4")
            .cmd("sh", "-c", "cat /etc/hosts | grep extrahost")
            .build();

    @Test
    public void shouldAddExtraHost() throws InterruptedException, IOException, DockerException {
        testee.waitForExit();
        String output = testee.getLog();
        assertThat(output, containsString("1.2.3.4"));
    }

}
