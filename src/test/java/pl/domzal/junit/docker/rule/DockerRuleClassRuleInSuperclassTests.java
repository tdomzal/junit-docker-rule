package pl.domzal.junit.docker.rule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeoutException;

import org.junit.ClassRule;
import org.junit.Test;

import pl.domzal.junit.docker.rule.DockerRule;

public class DockerRuleClassRuleInSuperclassTests {

    @ClassRule
    public static DockerRule testee = DockerRule.builder()//
            .imageName("busybox")//
            .cmd("sh", "-c", "echo 12345678")//
            .waitForMessage("12345678")
            .build();

    public static Ble a = new Ble();

    public static class Ble {
        public Ble() {
            System.out.println("------------- BLE -------------");
        }
    }

    public static class TestCase1 extends DockerRuleClassRuleInSuperclassTests {
        @Test
        public void shouldEcho1() throws TimeoutException, InterruptedException {
            testee.waitFor("12345678", 10);
            assertThat(testee.getLog(), containsString("12345678"));
        }
    }

    public static class TestCase2 extends DockerRuleClassRuleInSuperclassTests {
        @Test
        public void shouldEcho2() throws TimeoutException, InterruptedException {
            testee.waitFor("12345678", 10);
            assertThat(testee.getLog(), containsString("12345678"));
        }
    }
}
