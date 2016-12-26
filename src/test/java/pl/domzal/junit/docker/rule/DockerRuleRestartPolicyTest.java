package pl.domzal.junit.docker.rule;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.exceptions.DockerException;

@Category(test.category.Stable.class)
public class DockerRuleRestartPolicyTest {

    private Logger log = LoggerFactory.getLogger(DockerRuleRestartPolicyTest.class);

    @Rule
    public DockerRule testee = DockerRule.builder()
            .imageName("alpine:3.4")
            .restartPolicy(RestartPolicy.always())
            .cmd("sh", "-c", "sleep 2")
            .build();

    @Test
    public void shouldRestartAfterEnd() throws InterruptedException, IOException, DockerException {
        final String initialStartedUp = testee.getDockerClient().inspectContainer(testee.getContainerId()).state().startedAt().toString();
        new WaitForUnit(TimeUnit.SECONDS, 10, new WaitForUnit.WaitForCondition() {
            @Override
            public boolean isConditionMet() {
                try {
                    String currentStartedAt = testee.getDockerClient().inspectContainer(testee.getContainerId()).state().startedAt().toString();
                    log.debug("(initial) '{}' != (current) '{}' ?", initialStartedUp, currentStartedAt);
                    return ! initialStartedUp.equals(currentStartedAt);
                } catch (DockerException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
