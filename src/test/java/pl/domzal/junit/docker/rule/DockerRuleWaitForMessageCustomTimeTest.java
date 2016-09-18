package pl.domzal.junit.docker.rule;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

@Category(test.category.Stable.class)
public class DockerRuleWaitForMessageCustomTimeTest {

    private static Logger log = LoggerFactory.getLogger(DockerRuleWaitForMessageCustomTimeTest.class);

    public static final int WAIT_FOR_MESSAGE_SHORTER_THAN_DEFAULT = 5;

    @Test
    public void shouldWaitForLogMessage() throws Throwable {

        DockerRule testee = DockerRule.builder()//
                .imageName("busybox")//
                .cmd("sh", "-c", "for i in 01 02 03 05 06 07 08 09 10; do (echo $i; sleep 1); done")//
                .waitFor(WaitFor.logMessage("20"))
                .waitForTimeout(WAIT_FOR_MESSAGE_SHORTER_THAN_DEFAULT)
                .build();

        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            testee.before();
            fail("startup should timeout");
        } catch (TimeoutException te) {
            //expected
            assertThat( //
                    "container log should not contain message printed after timeout", //
                    testee.getLog(), not(containsString("10")));
        } finally {
            testee.after();
        }

        stopwatch.stop();
        assertTrue( //
                "wait time has been redifinded to shorter but container seems to be falling after default time anyway", //
                stopwatch.elapsed(TimeUnit.SECONDS) < DockerRuleBuilder.WAIT_FOR_DEFAULT_SECONDS);
    }

}
