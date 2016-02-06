package pl.domzal.junit.docker.rule;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(test.category.Stable.class)
public class DockerRuleExposePortUnexposedCheckTest {

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .imageName("nginx")//
            .expose("8125", "81")//
            .build();

    @Test(expected = IllegalStateException.class)
    public void shouldRaiseExceptionOnGetUnexposedPort() throws InterruptedException, IOException {
        testee.getExposedContainerPort("80");
    }

}
