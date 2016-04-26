package pl.domzal.junit.docker.rule.ex;

import java.util.concurrent.TimeoutException;

public class WaitTimeoutException extends TimeoutException {

    private final long waited;

    public WaitTimeoutException(String message, long waited) {
        super(message);
        this.waited = waited;
    }

    public long getWaited() {
        return waited;
    }
}
