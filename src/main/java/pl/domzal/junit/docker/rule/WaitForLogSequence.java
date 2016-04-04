package pl.domzal.junit.docker.rule;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@Link LineListener} that stops in {@link #waitForSequence()} till log source
 * will provide defined line sequence.
 */
class WaitForLogSequence implements DockerLogs.LineListener {

    private static Logger log = LoggerFactory.getLogger(WaitForLogSequence.class);

    private final List<String> logSequence;
    private final int timeoutSeconds;

    private AtomicInteger currentIndex = new AtomicInteger();

    WaitForLogSequence(List<String> logSequence, int timeoutSeconds) {
        this.logSequence = logSequence;
        this.timeoutSeconds = timeoutSeconds;
    }

    boolean sequenceFound() {
        return currentIndex.get() >= logSequence.size();
    }

    @Override
    public void nextLine(String line) {
        if (!sequenceFound()) {
            int currentLineIndex = currentIndex.get();
            String waitForLine = logSequence.get(currentLineIndex);
            if (line.contains(waitForLine)) {
                log.info("pattern {}:'{}' found in '{}'", currentLineIndex, waitForLine, line);
                currentIndex.incrementAndGet();
            } else {
                log.trace("pattern {}:'{}' not found", currentLineIndex, waitForLine);

            }
        }
    }

    public void waitForSequence() throws TimeoutException {
        try {
            log.info("wait for sequence {} started", logSequence);
            new WaitForUnit(TimeUnit.SECONDS, timeoutSeconds, TimeUnit.SECONDS, 1, new WaitForUnit.WaitForCondition() {
                @Override
                public boolean isConditionMet() {
                    return sequenceFound();
                }
            }).startWaiting();
            log.info("wait for sequence - all lines found");
        } catch (InterruptedException e) {
            throw new IllegalStateException(String.format("Interrupted while waiting for sequence %s", logSequence), e);
        }
    }

}
