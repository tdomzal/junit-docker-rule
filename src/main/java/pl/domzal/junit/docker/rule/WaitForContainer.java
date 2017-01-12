package pl.domzal.junit.docker.rule;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.domzal.junit.docker.rule.wait.StartConditionCheck;

/**
 * Helper class to block on given {@link WaitForContainer}.
 */
class WaitForContainer {

    private static Logger log = LoggerFactory.getLogger(WaitForContainer.class);

    /**
     * Wait till all given conditions are met.
     *
     * @param condition Conditions to wait for - all must be met to continue.
     * @param timeoutSeconds Wait timeout.
     * @param containerDescription Container description. For log and exception message usage only.
     */
    static void waitForCondition(final StartConditionCheck condition, int timeoutSeconds, final String containerDescription) throws TimeoutException {
        try {
            log.info("wait for {} started", condition.describe());
            new WaitForUnit(TimeUnit.SECONDS, timeoutSeconds, TimeUnit.SECONDS, 1, new WaitForUnit.WaitForCondition() {
                @Override
                public boolean isConditionMet() {
                    return condition.check();
                }
                @Override
                public String timeoutMessage() {
                    return String.format("timeout waiting for %s in container %s", condition.describe(), containerDescription);
                }
            }).startWaiting();
            log.info("wait for {} - condition met", condition.describe());
        } catch (InterruptedException e) {
            throw new IllegalStateException(String.format("Interrupted while waiting for %s", condition.describe()), e);
        }
    }

}
