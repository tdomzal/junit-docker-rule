package pl.domzal.junit.docker.rule.wait;

/**
 * Container startup condition.
 */
public interface WaitChecker {

    /**
     * Condition fulfilled check.
     */
    boolean check();

    /**
     * Condition description (for log messages).
     */
    String describe();

    /**
     * After check cleanup.
     */
    void after();

}