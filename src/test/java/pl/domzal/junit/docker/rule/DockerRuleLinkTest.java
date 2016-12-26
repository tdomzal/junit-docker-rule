package pl.domzal.junit.docker.rule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

@Category(test.category.Stable.class)
public class DockerRuleLinkTest {

    private static String DB_NAME = StringUtils.right(UUID.randomUUID().toString(), 8).toUpperCase();

    private static DockerRule db = DockerRule.builder()//
            .imageName("busybox:1.25.1")//
            .name(DB_NAME)//
            .cmd("sh", "-c", "sleep 30")//
            .build();

    private static DockerRule web = DockerRule.builder()//
            .imageName("busybox:1.25.1")//
            .name("web")//
            .link(DB_NAME)//
            .cmd("sh", "-c", "env")//
            .build();

    @ClassRule
    public static RuleChain containers = RuleChain.outerRule(db).around(web);

    @Test
    public void shouldPassEnvVariables() throws Throwable {
        web.waitForExit();
        String output = web.getLog();
        // env variable '/name_NAME=/web/name' is set when link is available
        assertThat(output, containsString(DB_NAME +"_NAME=/web/"+ DB_NAME));
    }

}
