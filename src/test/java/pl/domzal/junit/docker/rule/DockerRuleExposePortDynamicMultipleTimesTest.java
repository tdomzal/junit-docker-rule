package pl.domzal.junit.docker.rule;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(test.category.Stable.class)
public class DockerRuleExposePortDynamicMultipleTimesTest {

    private static Logger log = LoggerFactory.getLogger(DockerRuleExposePortDynamicMultipleTimesTest.class);

    @ClassRule
    public static DockerRule testee = DockerRule.builder()
            .imageName("nginx:1.10.2")
            // this is valid docker command:
            //      docker run -d -p "80" -p "80" ...
            .expose("80")
            .expose("80")
            .build();

    @Test
    public void exposeSamePortMultipleTimesIsAccepted() throws InterruptedException, IOException {
        String homepage = "http://"+testee.getDockerHost()+":"+testee.getExposedContainerPort("80")+"/";
        log.info("homepage: {}", homepage);
        assertTrue(AssertHtml.pageContainsString(homepage, "Welcome to nginx!"));
    }

}
