package pl.domzal.junit.docker.rule;

import java.util.concurrent.TimeoutException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(test.category.Stable.class)
public class DockerRuleEntrypointTest {

    @Rule
    public DockerRule testee = DockerRule.builder()//
            .imageName("busybox")//
            .extraHosts("extrahost:1.2.3.4")
            .entrypoint("/bin/echo", "12345")
            .build();

    @Test
    public void shouldDefineEntrypoint() throws TimeoutException, InterruptedException {
        testee.waitFor("12345", 5);
    }


}
