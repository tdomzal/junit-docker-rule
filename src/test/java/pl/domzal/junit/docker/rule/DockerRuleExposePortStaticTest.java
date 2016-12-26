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
public class DockerRuleExposePortStaticTest {

    private static Logger log = LoggerFactory.getLogger(DockerRuleExposePortStaticTest.class);

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .imageName("nginx:1.10.2")//
            .expose("8124", "80")//
            .build();

    private String nginxHome;

    @Before
    public void setupHomepage() {
        nginxHome = "http://"+testee.getDockerHost()+":8124/";
        log.info("homepage: {}", nginxHome);
    }

    @Test
    public void shouldExposeSpecifiedPort() throws InterruptedException, IOException {
        assertTrue(AssertHtml.pageContainsString(nginxHome, "Welcome to nginx!"));
    }

}
