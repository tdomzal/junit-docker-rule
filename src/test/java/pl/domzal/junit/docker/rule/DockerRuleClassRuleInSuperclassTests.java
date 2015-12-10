package pl.domzal.junit.docker.rule;

import java.util.concurrent.TimeoutException;

import org.junit.ClassRule;
import org.junit.Test;

public class DockerRuleClassRuleInSuperclassTests {

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .imageName("busybox")//
            .cmd("sh", "-c", "echo 12345678")//
            .waitForMessage("12345678")
            .build();

    public static class TestCase1 extends DockerRuleClassRuleInSuperclassTests {
        @Test
        public void shouldRun1() throws TimeoutException, InterruptedException {
        }
    }

    public static class TestCase2 extends DockerRuleClassRuleInSuperclassTests {
        @Test
        public void shouldRun2() throws TimeoutException, InterruptedException {
        }
    }
}
