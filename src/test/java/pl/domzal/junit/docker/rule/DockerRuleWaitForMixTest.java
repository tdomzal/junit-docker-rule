package pl.domzal.junit.docker.rule;

import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(test.category.Unstable.class)
public class DockerRuleWaitForMixTest {

    @Test
    @Ignore("todo")
    public void shouldWaitForAll() throws Throwable {
        fail();
    }

}
