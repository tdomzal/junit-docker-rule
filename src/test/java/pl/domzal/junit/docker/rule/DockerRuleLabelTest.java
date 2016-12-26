package pl.domzal.junit.docker.rule;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.spotify.docker.client.messages.ContainerInfo;

@Category(test.category.Stable.class)
public class DockerRuleLabelTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final PrintStream outWriter = new PrintStream(out, true);

    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final PrintStream errWriter = new PrintStream(err, true);

    @Test
    public void shouldLabelContainerSingleLabel() throws Throwable {
        DockerRule testee = DockerRule.builder()//
                .imageName("busybox:1.25.1")//
                .cmd("sh", "-c", "echo 01stdout; sleep 100")//
                .addLabel("label-key", "label-value")
                .build();
        testee.before();
        try {

            ContainerInfo containerInfo = testee.getDockerClient().inspectContainer(testee.getContainerId());
            Map<String, String> labels = containerInfo.config().labels();

            assertTrue(labels.containsKey("label-key"));
            assertEquals("label-value", labels.get("label-key"));

        } finally {
            testee.after();
        }
    }

}
