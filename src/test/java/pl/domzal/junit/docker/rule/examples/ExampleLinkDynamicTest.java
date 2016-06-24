package pl.domzal.junit.docker.rule.examples;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

import pl.domzal.junit.docker.rule.DockerRule;

/**
 * Container linking. &quot;web&quot; links to &quot;db&quot; without specifying db name and pings it.
 */
@Category(test.category.Stable.class)
public class ExampleLinkDynamicTest {

    private static DockerRule dbRule = DockerRule.builder()//
            .imageName("alpine")//
            .cmd("sh", "-c", "sleep 30")//
            .build();

    private static DockerRule web = DockerRule.builder()//
            .imageName("alpine")//
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
