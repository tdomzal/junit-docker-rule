package pl.domzal.junit.docker.rule;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.AssertionFailedError;

public class WaitForLogSequenceWaitingTest {

    private static Logger log = LoggerFactory.getLogger(WaitForLogSequenceWaitingTest.class);

    public static final int WAIT_LOG_TIMEOUT = 1000;
    public static final int WAIT_FOR_TIMEOUT_TIMEOUT = 3 * WAIT_LOG_TIMEOUT;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final AtomicBoolean doneWaiting = new AtomicBoolean(false);
    private final AtomicBoolean timeoutWaiting = new AtomicBoolean(false);
    private final AtomicReference<Throwable> exceptionWaiting = new AtomicReference<>();

    class WaitRunner implements Runnable {

        private final WaitForLogSequence waitForLogSequence;

        WaitRunner(WaitForLogSequence waitForLogSequence) {
            this.waitForLogSequence = waitForLogSequence;
        }

        @Override
        public void run() {
            log.debug("runner started");
            try {
                waitForLogSequence.waitForSequence();
                doneWaiting.set(true);
            } catch (TimeoutException e) {
                timeoutWaiting.set(true);
            } catch (Throwable e) {
                exceptionWaiting.set(e);
            }
            log.debug("runner finished");
        }
    }

    @Test(timeout = 10000)
    public void shouldStopAfterWholeSequence() throws Exception {
        WaitForLogSequence testee = new WaitForLogSequence(Arrays.asList("one", "three"), WAIT_LOG_TIMEOUT);
        executor.submit(new WaitRunner(testee));
        testee.nextLine("one");
        testee.nextLine("two");
        testee.nextLine("three");
        waitForDone();
        assertNoExceptionInWorker();
    }

    @Test(timeout = 10000)
    public void shouldTimeoutOnPartSequence() throws Exception {
        WaitForLogSequence testee = new WaitForLogSequence(Arrays.asList("one", "three"), WAIT_LOG_TIMEOUT);
        executor.submit(new WaitRunner(testee));
        testee.nextLine("one");
        testee.nextLine("two");
        waitForTimeout();
        assertNoExceptionInWorker();
    }

    @Test(timeout = 10000)
    public void shouldNotWaitOnEmptySequence() throws Exception {
        WaitForLogSequence testee = new WaitForLogSequence(Arrays.<String>asList(), WAIT_LOG_TIMEOUT);
        executor.submit(new WaitRunner(testee));
        waitForDone();
        assertNoExceptionInWorker();
    }

    private void waitForDone() throws TimeoutException, InterruptedException {
        new WaitForUnit(TimeUnit.SECONDS, 1, new WaitForUnit.WaitForCondition() {
            @Override
            public boolean isConditionMet() {
                return doneWaiting.get();
            }
        }).startWaiting();
    }

    private void waitForTimeout() throws TimeoutException, InterruptedException {
        new WaitForUnit(TimeUnit.SECONDS, WAIT_FOR_TIMEOUT_TIMEOUT, TimeUnit.SECONDS, 1, new WaitForUnit.WaitForCondition() {
            @Override
            public boolean isConditionMet() {
                return timeoutWaiting.get();
            }
            @Override
            public String tickMessage() {
                return "wait for timeout: "+super.tickMessage();
            }
        }).startWaiting();
    }

    private void assertNoExceptionInWorker() {
        if (exceptionWaiting.get() != null) {
            throw new AssertionFailedError("wait failed with exception: "+ exceptionWaiting.get().getMessage());
        }
    }

    @After
    public void teardown() throws InterruptedException {
        executor.awaitTermination(1, TimeUnit.SECONDS);
        executor.shutdown();
    }

}