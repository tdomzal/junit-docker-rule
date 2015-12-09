package pl.domzal.junit.docker.rule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import com.spotify.docker.client.DockerException;

public class DockerRuleEnvTest {

    @Rule
    public DockerRule testee = DockerRule.builder()//
            .imageName("busybox")//
            .env("EXTRA_OPT", "EXTRA_OPT_VALUE")
            .cmd("sh", "-c", "echo $EXTRA_OPT")//
            .build();

    @Test
    public void shouldPassEnvVariables() throws InterruptedException, IOException, DockerException {
        testee.waitForExit();
        String output = testee.getLog();
        assertThat(output, containsString("EXTRA_OPT_VALUE"));
    }


}
