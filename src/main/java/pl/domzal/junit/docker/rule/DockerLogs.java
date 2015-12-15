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

class DockerLogs implements Closeable {

    private static Logger log = LoggerFactory.getLogger(DockerLogs.class);

    private static final int SHORT_ID_LEN = 12;

    private final DockerClient dockerClient;
    private final String containerId;

    private PrintStream stdoutWriter = System.out;
    private PrintStream stderrWriter = System.err;

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()//
            .setNameFormat("dockerlog-pool-%d")//
            .setDaemon(true)//
            .build();

    private final ExecutorService executor = Executors.newFixedThreadPool(3, threadFactory);

    DockerLogs(DockerClient dockerClient, String containerId) {
        this.dockerClient = dockerClient;
        this.containerId = containerId;
    }

    public void setStderrWriter(PrintStream stderrWriter) {
        this.stderrWriter = stderrWriter;
    }

    public void setStdoutWriter(PrintStream stdoutWriter) {
        this.stdoutWriter = stdoutWriter;
    }

    public void start() throws IOException, InterruptedException {

        String shortId = StringUtils.left(containerId, SHORT_ID_LEN);

        final PipedInputStream stdoutInputStream = new PipedInputStream();
        final PipedInputStream stderrInputStream = new PipedInputStream();

        final PipedOutputStream stdoutPipeOutputStream = new PipedOutputStream(stdoutInputStream);
        final PipedOutputStream stderrPipeOutputStream = new PipedOutputStream(stderrInputStream);

        executor.submit(new IsPrinter(shortId+"-stdout> ", stdoutInputStream, stdoutWriter));
        executor.submit(new IsPrinter(shortId+"-stderr> ", stderrInputStream, stderrWriter));
        executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LogStream logs = dockerClient.logs(containerId, LogsParam.stdout(), LogsParam.stderr(), LogsParam.follow());
                logs.attach(stdoutPipeOutputStream, stderrPipeOutputStream);
                logs.close();
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

    class IsPrinter implements Runnable {

        private final String prefix;
        private final InputStream inputStream;
        private final PrintStream output;

        IsPrinter(String prefix, InputStream inputStream, PrintStream output) {
            this.prefix = prefix;
            this.inputStream = inputStream;
            this.output = output;
        }

        @Override
        public void run() {
            try (Scanner scanner = new Scanner(inputStream)) {
                while (scanner.hasNextLine()) {
                    output.println(prefix + scanner.nextLine());
                }
            }
        }
    }

}
