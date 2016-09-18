package pl.domzal.junit.docker.rule.wait;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link StartConditionCheck} met when incoming log lines contains specified
 * message sequence..
 */
public class LogSequenceChecker implements LineListener, StartConditionCheck {

    private static Logger log = LoggerFactory.getLogger(LogSequenceChecker.class);

    private final List<String> logSequence;

    private AtomicInteger currentIndex = new AtomicInteger();

    public LogSequenceChecker(List<String> logSequence) {
        this.logSequence = logSequence;
    }

    @Override
    public boolean check() {
        return currentIndex.get() >= logSequence.size();
    }

    @Override
    public String describe() {
        return String.format("log sequence %s", logSequence);
    }

    @Override
    public void after() { }

    @Override
    public void nextLine(String line) {
        if (!check()) {
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

}
