package pl.domzal.junit.docker.rule.wait;

/**
 * Container startup condition check. Instances of check should
 * be created by implementations of {@link StartCondition}.
 */
public interface StartConditionCheck {

    /**
     * 'Is condition fulfilled?' check.
     */
    boolean check();

    /**
     * Condition description (will show in log messages).
     */
    String describe();

    /**
     * After check cleanup. Use if you check need to do some cleaning after usage.
     */
    void after();

}