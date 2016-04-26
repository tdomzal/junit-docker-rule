package pl.domzal.junit.docker.rule.wait;

/**
 * Something that listens for log lines.
 */
public interface LineListener {
    void nextLine(String line);
}
