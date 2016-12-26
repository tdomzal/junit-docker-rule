package pl.domzal.junit.docker.rule;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(test.category.Stable.class)
public class DockerRuleExposePortExposeAllTest {

    private static Logger log = LoggerFactory.getLogger(DockerRuleExposePortExposeAllTest.class);

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .imageName("nginx:1.10.2")//
            .build();

    private String nginxHome;

    @Before
    public void setupHomepage() {
        nginxHome = "http://"+testee.getDockerHost()+":"+testee.getExposedContainerPort("80")+"/";
        log.info("homepage: {}", nginxHome);
    }

    @Test
    public void shouldExposeDynamicPortHttpPort() throws InterruptedException, IOException {
        assertTrue(AssertHtml.pageContainsString(nginxHome, "Welcome to nginx!"));
    }

}
