package pl.domzal.junit.docker.rule;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogStream;

/**
 * Docker container log binding feature.
 */
class DockerLogs implements Closeable {

    private static Logger log = LoggerFactory.getLogger(DockerLogs.class);

    private static final int SHORT_ID_LEN = 12;

    private final DockerClient dockerClient;
    private final String containerId;
    private final LineListener lineListener;

    private PrintStream stdoutWriter = System.out;
    private PrintStream stderrWriter = System.err;

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()//
            .setNameFormat("dockerlog-pool-%d")//
            .setDaemon(true)//
            .build();

    private final ExecutorService executor = Executors.newFixedThreadPool(3, threadFactory);

    DockerLogs(DockerClient dockerClient, String containerId, LineListener lineListener) {
        this.dockerClient = dockerClient;
        this.containerId = containerId;
        this.lineListener = lineListener;
    }

    public void setStderrWriter(PrintStream stderrWriter) {
        this.stderrWriter = stderrWriter;
    }

    public void setStdoutWriter(PrintStream stdoutWriter) {
        this.stdoutWriter = stdoutWriter;
    }

    public void start() throws IOException, InterruptedException {

        final String containerShortId = StringUtils.left(containerId, SHORT_ID_LEN);

        final PipedInputStream stdoutInputStream = new PipedInputStream();
        final PipedInputStream stderrInputStream = new PipedInputStream();

        final PipedOutputStream stdoutPipeOutputStream = new PipedOutputStream(stdoutInputStream);
        final PipedOutputStream stderrPipeOutputStream = new PipedOutputStream(stderrInputStream);

        executor.submit(new LogPrinter(containerShortId+"-stdout> ", stdoutInputStream, stdoutWriter, lineListener));
        executor.submit(new LogPrinter(containerShortId+"-stderr> ", stderrInputStream, stderrWriter, lineListener));
        executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                log.trace("{} attaching to logs", containerShortId);
                LogStream logs = dockerClient.logs(containerId, LogsParam.stdout(), LogsParam.stderr(), LogsParam.follow());
                logs.attach(stdoutPipeOutputStream, stderrPipeOutputStream);
                logs.close();
                log.trace("{} dettached from logs", containerShortId);
                return null;
            }
        });
    }

    @Override
    public void close() {
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("interrupted", e);
        }
        executor.shutdown();
    }

    /**
     * Something that listens for log lines.
     */
    public interface LineListener {
        void nextLine(String line);
    }

    static class LogPrinter implements Runnable {

        private final String prefix;
        private final InputStream scannedInputStream;
        private final PrintStream output;
        private final LineListener lineListener;

        LogPrinter(String prefix, InputStream scannedInputStream, PrintStream output, LineListener lineListener) {
            this.prefix = prefix;
            this.scannedInputStream = scannedInputStream;
            this.output = output;
            this.lineListener = lineListener;
        }

        @Override
        public void run() {
            log.trace("{} printer thread started", prefix);
            try (Scanner scanner = new Scanner(scannedInputStream)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    log.trace("{} line: {}", prefix, line);
                    output.println(prefix + line);
                    if (lineListener != null) {
                        lineListener.nextLine(line);
                    }
                }
            }
            log.trace("{} printer thread terminated", prefix);
        }
    }

}
