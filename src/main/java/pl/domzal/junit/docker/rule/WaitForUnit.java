package pl.domzal.junit.docker.rule;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wait for condition ({@link WaitForCondition}) for time specified in {@link java.util.concurrent.TimeUnit}s.
 */
class WaitForUnit {

    /**
     * Tick time between wait for condition check [ms].
     */
    public static final int DEFAULT_TICK_TIME_MS = 50;

    private static Logger log = LoggerFactory.getLogger(WaitForUnit.class);

    private final long waitMs;
    private final long tickMs;
    private final long deadlineTime;
    private final long startTime;
    private final WaitForCondition doneWaitingCondition;

    /**
     * @param timeUnit Wait time unit
     * @param waitTime Wait time (for condition)
     * @param condition Condition we are waiting for
     */
    public WaitForUnit(TimeUnit timeUnit, int waitTime, WaitForCondition condition) {
        this(timeUnit, waitTime, TimeUnit.MILLISECONDS, DEFAULT_TICK_TIME_MS, condition);
    }

    /**
     * @param timeUnit Time unit for wait time and check interval time
     * @param waitTime Wait time (for condition)
     * @param tickTime Check time interval
     * @param condition Condition we are waiting for
     */
    public WaitForUnit(TimeUnit timeUnit, int waitTime, int tickTime, WaitForCondition condition) {
        this(timeUnit, waitTime, timeUnit, tickTime, condition);
    }

    /**
     * @param waitUnit Wait time unit
     * @param waitTime Wait time (for condition)
     * @param tickUnit Check time interval unit
     * @param tickTime Check time interval
     * @param condition Condition we are waiting for
     */
    public WaitForUnit(TimeUnit waitUnit, int waitTime, TimeUnit tickUnit, int tickTime, WaitForCondition condition) {
        this.waitMs = waitUnit.toMillis(waitTime);
        this.tickMs = tickUnit.toMillis(tickTime);
        this.startTime = System.currentTimeMillis();
        this.deadlineTime = this.startTime+this.waitMs;
        this.doneWaitingCondition = condition;
    }

    public void startWaiting() throws TimeoutException, InterruptedException {
        String conditionDescription = doneWaitingCondition.tickMessage();
        log.debug("{} - started ({}ms)", conditionDescription, waitMs);
        while (true) {
            long currentTime = System.currentTimeMillis();
            conditionDescription = doneWaitingCondition.tickMessage();
            if (doneWaitingCondition.isConditionMet()) {
                log.debug("{} - condition met in {} ms", conditionDescription, (currentTime - startTime));
                return;
            } else {
                log.trace("{} - waiting...", conditionDescription);
            }
            TimeUnit.MILLISECONDS.sleep(tickMs);
            currentTime = System.currentTimeMillis();
            assertTimeNotExceeded(conditionDescription, currentTime);
        }
    }

    private void assertTimeNotExceeded(String waitForConditionDescription, long currentTime) throws TimeoutException {
        if (currentTime > deadlineTime) {
            String timeoutTick = doneWaitingCondition.timeoutMessage();
            log.warn("wait failed with {}", timeoutTick);
            String errorMessage = "Condition ["+waitForConditionDescription+"] was not met for [" + (currentTime-startTime) + "/"+waitMs+"]ms, "+timeoutTick;
            // error visible in console
            log.error(errorMessage);
            // exception for junit test to fall miserably
            throw new TimeoutException(errorMessage);
        }
    }

    public static abstract class WaitForCondition {

        public abstract boolean isConditionMet();

        /**
         * Part of message shown on every tick.
         * Can be overriden to extend diagnostic information in log.
         */
        public String tickMessage() {
            return "wait...";
        }

        /**
         * Part of message shown on wait timeout.
         * Can be overriden to extend diagnostics information in log and in thrown {@link TimeoutException} instance.
         */
        public String timeoutMessage() {
            return "timeout...";
        }
    }
}

