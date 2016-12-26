package pl.domzal.junit.docker.rule.logs;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.domzal.junit.docker.rule.wait.LineListener;

public class LogPrinter implements Runnable {

    private static Logger log = LoggerFactory.getLogger(LogPrinter.class);

    private final String prefix;
    private final InputStream scannedInputStream;
    private final PrintStream output;
    private final LineListener lineListener;

    public LogPrinter(String prefix, InputStream scannedInputStream, PrintStream output, LineListener lineListener) {
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
                if (output != null) {
                    output.println(prefix + line);
                }
                if (lineListener != null) {
                    lineListener.nextLine(line);
                }
            }
        }
        log.trace("{} printer thread terminated", prefix);
    }
}
