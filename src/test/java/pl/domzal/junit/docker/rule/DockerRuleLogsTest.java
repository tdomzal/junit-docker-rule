package pl.domzal.junit.docker.rule;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class DockerRuleLogsTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final PrintStream outWriter = new PrintStream(out, true);

    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final PrintStream errWriter = new PrintStream(err, true);

    @Test
    public void shouldWriteStdout() throws Throwable {
        DockerRule testee = DockerRule.builder()//
                .imageName("busybox")//
                .cmd("sh", "-c", "echo 01stdout")//
                .stdoutWriter(outWriter)
                .stderrWriter(errWriter)
                .build();
        testee.before();
        try {
            testee.waitForExit();
            assertThat(out.toString(StandardCharsets.UTF_8.name()), containsString("01stdout"));
            assertThat(err.toString(StandardCharsets.UTF_8.name()), not(containsString("01stdout")));
        } finally {
            testee.after();
        }
    }

    @Test
    public void shouldWriteStderr() throws Throwable {
        DockerRule testee = DockerRule.builder()//
                .imageName("busybox")//
                .cmd("sh", "-c", ">&2 echo 02stderr")//
                .stdoutWriter(outWriter)
                .stderrWriter(errWriter)
                .build();
        testee.before();
        try {
            testee.waitForExit();
            assertThat(err.toString(StandardCharsets.UTF_8.name()), containsString("02stderr"));
            assertThat(out.toString(StandardCharsets.UTF_8.name()), not(containsString("02stderr")));
        } finally {
            testee.after();
        }
    }

}
