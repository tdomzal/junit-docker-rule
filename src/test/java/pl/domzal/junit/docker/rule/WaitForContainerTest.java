package pl.domzal.junit.docker.rule;

import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.AssertionFailedError;
import pl.domzal.junit.docker.rule.wait.StartConditionCheck;

@Category(test.category.Stable.class)
public class WaitForContainerTest {

    private static Logger log = LoggerFactory.getLogger(WaitForContainerTest.class);

    public static final int WAIT_LOG_TIMEOUT_SEC = 4;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final AtomicBoolean doneWaiting = new AtomicBoolean(false);
    private final AtomicBoolean timeoutWaiting = new AtomicBoolean(false);
    private final AtomicReference<Throwable> exceptionWaiting = new AtomicReference<>();

    class WaitRunner implements Runnable {

        private final StartConditionCheck condition;

        WaitRunner(StartConditionCheck condition) {
            this.condition = condition;
        }

        @Override
        public void run() {
            log.debug("runner started");
            try {
                WaitForContainer.waitForCondition(condition, WAIT_LOG_TIMEOUT_SEC);
                doneWaiting.set(true);
            } catch (TimeoutException e) {
                timeoutWaiting.set(true);
            } catch (Throwable e) {
                exceptionWaiting.set(e);
            }
            log.debug("runner finished");
        }
    }

    @Test(timeout = 20000)
    public void shouldStopWhenConditionMet() throws Exception {
        StartConditionCheck condition = mock(StartConditionCheck.class);
        when(condition.check()).thenReturn(true);
        executor.submit(new WaitRunner(condition));
        waitForDone();
        assertNoExceptionInWorker();
    }

    @Test(timeout = 10000)
    public void shouldTimeoutWhenConditionNotMet() throws Exception {
        StartConditionCheck condition = mock(StartConditionCheck.class);
        when(condition.check()).thenReturn(false);
        executor.submit(new WaitRunner(condition));
        waitForTimeout();
        assertNoExceptionInWorker();
    }

    @Test(timeout = 10000, expected = IllegalStateException.class)
    public void shouldRethrowException() throws Exception {
        StartConditionCheck condition = mock(StartConditionCheck.class);
        when(condition.check()).thenThrow(new IllegalStateException("kaboom"));
        WaitForContainer.waitForCondition(condition, WAIT_LOG_TIMEOUT_SEC);
    }

    private void waitForDone() throws TimeoutException, InterruptedException {
        new WaitForUnit(TimeUnit.SECONDS, 5, new WaitForUnit.WaitForCondition() {
            @Override
            public boolean isConditionMet() {
                return doneWaiting.get();
            }
        }).startWaiting();
    }

    private void waitForTimeout() throws TimeoutException, InterruptedException {
        new WaitForUnit(TimeUnit.SECONDS, 10, TimeUnit.SECONDS, 1, new WaitForUnit.WaitForCondition() {
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