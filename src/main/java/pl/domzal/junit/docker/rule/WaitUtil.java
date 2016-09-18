package pl.domzal.junit.docker.rule;

import java.util.Arrays;

import pl.domzal.junit.docker.rule.ex.WaitTimeoutException;
import pl.domzal.junit.docker.rule.wait.StartConditionCheck;

public class WaitUtil {

    // how long to wait at max when doing a http ping
    private static final long DEFAULT_MAX_WAIT = 10 * 1000;

    // How long to wait between pings
    private static final long WAIT_RETRY_WAIT = 500;

    private WaitUtil() {}

    public static long wait(int maxWait, StartConditionCheck... checkers) throws WaitTimeoutException {
        return wait(maxWait, Arrays.asList(checkers));
    }

    public static long wait(int maxWait, Iterable<StartConditionCheck> checkers) throws WaitTimeoutException {
        long max = maxWait > 0 ? maxWait : DEFAULT_MAX_WAIT;
        long now = System.currentTimeMillis();
        try {
            do {
                for (StartConditionCheck checker : checkers) {
                    if (checker.check()) {
                        return delta(now);
                    }
                }
                sleep(WAIT_RETRY_WAIT);
            } while (delta(now) < max);

            throw new WaitTimeoutException("No checker finished successfully", delta(now));

        } finally {
            cleanup(checkers);
        }
    }

    // Give checkers a possibility to clean up
    private static void cleanup(Iterable<StartConditionCheck> checkers) {
        for (StartConditionCheck checker : checkers) {
            checker.after();
        }
    }

    /**
     * Sleep a bit
     *
     * @param millis how long to sleep in milliseconds
     */
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ...
        }
    }

    private static long delta(long now) {
        return System.currentTimeMillis() - now;
    }

}