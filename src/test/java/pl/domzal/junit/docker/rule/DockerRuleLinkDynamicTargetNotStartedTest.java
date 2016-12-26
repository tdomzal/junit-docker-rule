package pl.domzal.junit.docker.rule;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(test.category.Stable.class)
public class DockerRuleLinkDynamicTargetNotStartedTest {

    @Test(expected = IllegalStateException.class)
    public void shouldRaiseException() throws Throwable {
        DockerRule db = DockerRule.builder()//
                .imageName("alpine:3.4")//
                .cmd("sh", "-c", "sleep 30")//
                .build();

        DockerRule web = DockerRule.builder()//
                .imageName("alpine:3.4")//
                .link(db, "db")//
                .cmd("sh", "-c", "ping -w 1 db")//
                .build();

        // db not started before web should raise exception
        web.before();
    }

}
