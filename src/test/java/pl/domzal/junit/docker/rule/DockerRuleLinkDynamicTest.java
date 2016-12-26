package pl.domzal.junit.docker.rule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

@Category(test.category.Stable.class)
public class DockerRuleLinkDynamicTest {

    private static DockerRule dbRule = DockerRule.builder()//
            .imageName("alpine:3.4")//
            .cmd("sh", "-c", "sleep 30")//
            .build();

    private static DockerRule web = DockerRule.builder()//
            .imageName("alpine:3.4")//
            .link(dbRule, "db")//
            .cmd("sh", "-c", "ping -w 1 db")//
            .build();

    @ClassRule
    public static RuleChain containers = RuleChain.outerRule(dbRule).around(web);

    @Test
    public void shouldPingViaLink() throws Throwable {
        web.waitForExit();
        String output = web.getLog();
        assertThat(output, containsString("1 packets received"));
    }

}
