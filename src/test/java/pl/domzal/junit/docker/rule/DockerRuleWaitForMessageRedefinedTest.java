package pl.domzal.junit.docker.rule;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(test.category.Stable.class)
public class DockerRuleWaitForMessageRedefinedTest {

    @Test(expected = IllegalStateException.class)
    public void redefineWaitForMessageShouldRaiseError() throws Throwable {
        DockerRule httpd = DockerRule.builder() //
                .imageName("nginx")//
                .publishAllPorts(false)
                .waitForMessage("one")
                .waitForMessage("two")
                .build();
    }

}
