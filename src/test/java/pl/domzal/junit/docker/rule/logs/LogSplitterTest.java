package pl.domzal.junit.docker.rule.logs;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.domzal.junit.docker.rule.wait.LineListener;

public class LogSplitterTest {

    private static Logger log = LoggerFactory.getLogger(LogSplitterTest.class);

    public static final int NO_OF_LOG_LISTENERS = 3;

    private final ExecutorService executor = Executors.newFixedThreadPool(NO_OF_LOG_LISTENERS);

    private LineListener combinedLines = mock(LineListener.class);
    private LineListener stdoutLines = mock(LineListener.class);
    private LineListener stderrLines = mock(LineListener.class);

    LogSplitter testee = new LogSplitter();

    @Before
    public void before() {
        // out log printer as handy stream -> lines converter
        executor.submit(new LogPrinter("", testee.getCombinedInput(), null, combinedLines));
        executor.submit(new LogPrinter("", testee.getStdoutInput(), null, stdoutLines));
        executor.submit(new LogPrinter("", testee.getStderrInput(), null, stderrLines));
    }

    @Test
    public void shouldTeeStdout() throws IOException {
        // when
        OutputStream stdout = testee.getStdoutOutput();
        stdout.write("one\n".getBytes(StandardCharsets.UTF_8));
        stdout.flush();

        // then
        verify(combinedLines, timeout(1000)).nextLine("one");
        verify(stdoutLines, timeout(1000)).nextLine("one");
        verify(stderrLines, never()).nextLine(anyString());
    }

    @Test
    public void shouldTeeStderr() throws IOException {
        // when
        OutputStream stderr = testee.getStderrOutput();
        stderr.write("one\n".getBytes(StandardCharsets.UTF_8));
        stderr.flush();

        // then
        verify(combinedLines, timeout(1000)).nextLine("one");
        verify(stderrLines, timeout(1000)).nextLine("one");
        verify(stdoutLines, never()).nextLine(anyString());
    }

    @After
    public void after() {
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("interrupted", e);
        }
        executor.shutdown();
    }

}