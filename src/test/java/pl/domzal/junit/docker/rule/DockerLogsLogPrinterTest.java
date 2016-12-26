package pl.domzal.junit.docker.rule;

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InOrder;

import pl.domzal.junit.docker.rule.logs.LogPrinter;
import pl.domzal.junit.docker.rule.wait.LineListener;

@Category(test.category.Stable.class)
public class DockerLogsLogPrinterTest {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private PrintWriter containerStdout;
    private ByteArrayOutputStream logOutputStream;
    LineListener lineListener = mock(LineListener.class);

    LogPrinter testee;

    @Before
    public void setup() throws IOException {
        PipedInputStream containerStdoutIs = new PipedInputStream();
        containerStdout = new PrintWriter(new PipedOutputStream(containerStdoutIs));
        logOutputStream = new ByteArrayOutputStream();
        testee = new LogPrinter("prefix", containerStdoutIs, new PrintStream(logOutputStream), lineListener);
        executor.submit(testee);
    }

    @Test
    public void shouldPrintContainerOuputToLog() throws Exception {
        containerStdout.println("one");
        containerStdout.println("two");
        containerStdout.println("three");
        containerStdout.flush();

        new WaitForUnit(TimeUnit.SECONDS, 1, new WaitForUnit.WaitForCondition() {
            @Override
            public boolean isConditionMet() {
                return logOutput().contains("three");
            }
        }).startWaiting();
    }

    @Test
    public void shouldPassContainerOutputLinesToLineListener() {
        containerStdout.println("one");
        containerStdout.println("two");
        containerStdout.println("three");
        containerStdout.flush();

        verify(lineListener, timeout(1000).times(3)).nextLine(anyString());

        InOrder inOrder = inOrder(lineListener);
        inOrder.verify(lineListener).nextLine("one");
        inOrder.verify(lineListener).nextLine("two");
        inOrder.verify(lineListener).nextLine("three");
    }

    @Test
    public void shouldPassSingleLineOnMultipleLinePartPrints() {
        containerStdout.print("quite ");
        containerStdout.flush();
        containerStdout.print("long ");
        containerStdout.flush();
        containerStdout.println("line");
        containerStdout.flush();

        verify(lineListener, timeout(1000)).nextLine("quite long line");
    }

    @After
    public void teardown() throws InterruptedException {
        executor.awaitTermination(1, TimeUnit.SECONDS);
        executor.shutdown();
        System.out.println(logOutput());
    }

    /**
     * Output log collected till now.
     */
    private String logOutput() {
        try {
            return logOutputStream.toString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}