package pl.domzal.junit.docker.rule;

import static org.junit.Assert.*;

import java.util.concurrent.TimeoutException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(test.category.Stable.class)
public class DockerRuleWaitForLogMessageTest {

    @Rule
    public DockerRule testee = DockerRule.builder()//
            .imageName("busybox")//
            .cmd("sh", "-c", "for i in 01 02 03 04 05 06 07 08 09 10; do (echo $i; sleep 1); done")//
            .build();

    @Test(expected = TimeoutException.class)
    public void shouldWaitSpecifiedTimelimitWhenNotFound() throws InterruptedException, TimeoutException {
        int MAX_WAIT = 3;
        long start = System.currentTimeMillis();
        testee.waitFor("11", MAX_WAIT);
        long stop = System.currentTimeMillis();
        assertTrue("should wait specified number of seconds", start + (MAX_WAIT * 1000) + 1 > stop);
    }

    @Test(expected = TimeoutException.class)
    public void shouldNotWaitForContainerExitWhenMessageNotFoundInTimelimit() throws InterruptedException, TimeoutException {
        int MAX_WAIT = 3;
        long start = System.currentTimeMillis();
        testee.waitFor("11", MAX_WAIT);
        long stop = System.currentTimeMillis();
        assertTrue("should not wait for container stop", stop < start + (10 * 1000));
    }

    @Test
    public void shouldWaitForMessage() throws InterruptedException, TimeoutException {
        int MAX_WAIT = 10;
        testee.waitFor("03", MAX_WAIT);
        String logContent = testee.getLog();
        assertTrue("log does not contain message we are waiting for", logContent.contains("03"));
    }

    @Test
    public void shouldNotWaitForContainerExitWhenMessageFound() throws InterruptedException, TimeoutException {
        int MAX_WAIT = 10;
        long start = System.currentTimeMillis();
        testee.waitFor("03", MAX_WAIT);
        long stop = System.currentTimeMillis();
        assertTrue("should not wait for container stop", stop < start + (10 * 1000));
    }

}
