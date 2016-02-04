package pl.domzal.junit.docker.rule;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeoutException;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class DockerRuleClassRuleInSuperclassTests {

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .imageName("busybox")//
            .cmd("sh", "-c", "echo 12345678")//
            .waitForMessage("12345678")
            .build();

    private static String firstIdNotShared;

    public static class TestCase1 extends DockerRuleClassRuleInSuperclassTests {
        @Rule
        public TestName name = new TestName();
        @Test
        public void testRun1() throws TimeoutException, InterruptedException {
            assertContainerIsNotReused(testee.getContainerId());
        }
    }

    public static class TestCase2 extends DockerRuleClassRuleInSuperclassTests {
        @Rule
        public TestName name = new TestName();

        static String firstIdShared;

        @Test
        public void testRun21() throws TimeoutException, InterruptedException {
            assertContainerIsReused(testee.getContainerId());
            assertContainerIsNotReused(testee.getContainerId());
        }
        @Test
        public void testRun22() throws TimeoutException, InterruptedException {
            assertContainerIsReused(testee.getContainerId());
        }

        private synchronized void assertContainerIsReused(String containerId) {
            if (firstIdShared==null) {
                firstIdShared = testee.getContainerId();
            } else {
                assertThat("container defined with @ClassRule should be reused in same class", testee.getContainerId(), equalTo(firstIdShared));
            }
        }
    }

    private static synchronized void assertContainerIsNotReused(String containerId) {
        if (firstIdNotShared==null) {
            firstIdNotShared = testee.getContainerId();
        } else {
            assertThat("container defined with @ClassRule should be reused in same class", testee.getContainerId(), not(equalTo(firstIdNotShared)));
        }
    }
}
