package pl.domzal.junit.docker.rule.logs;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;

/**
 * Helper class for joining docker logs stdout and stderr streams
 * to set of listening input streams where one of listening input
 * streams will contain joined content of stdout and stderr.
 */
public class LogSplitter implements Closeable {

    private final PipedInputStream stdoutInput = new PipedInputStream();
    private final PipedInputStream stderrInput = new PipedInputStream();
    private final PipedInputStream combinedInput = new PipedInputStream();

    private final PipedOutputStream stdoutPipeOutputStream;
    private final PipedOutputStream stderrPipeOutputStream;
    private final PipedOutputStream combinedPipeOutputStream;

    private final OutputStream stdoutOutput;
    private final OutputStream stderrOutput;

    public LogSplitter() {
        try {
            this.stdoutPipeOutputStream = new PipedOutputStream(stdoutInput);
            this.stderrPipeOutputStream = new PipedOutputStream(stderrInput);
            this.combinedPipeOutputStream = new PipedOutputStream(combinedInput);
            this.stdoutOutput = new TeeOutputStream(stdoutPipeOutputStream, combinedPipeOutputStream);
            this.stderrOutput = new TeeOutputStream(stderrPipeOutputStream, combinedPipeOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create", e);
        }
    }

    public OutputStream getStdoutOutput() {
        return stdoutOutput;
    }

    public OutputStream getStderrOutput() {
        return stderrOutput;
    }

    public InputStream getStdoutInput() {
        return stdoutInput;
    }

    public InputStream getStderrInput() {
        return stderrInput;
    }

    public InputStream getCombinedInput() {
        return combinedInput;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(stderrInput);
        IOUtils.closeQuietly(stdoutInput);
        IOUtils.closeQuietly(combinedInput);
        IOUtils.closeQuietly(stdoutPipeOutputStream);
        IOUtils.closeQuietly(stderrPipeOutputStream);
        IOUtils.closeQuietly(combinedPipeOutputStream);
        IOUtils.closeQuietly(stdoutOutput);
        IOUtils.closeQuietly(stderrOutput);
    }
}
