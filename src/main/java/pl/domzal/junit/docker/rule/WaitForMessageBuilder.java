package pl.domzal.junit.docker.rule;

import java.util.ArrayList;

public class WaitForMessageBuilder {

    private final DockerRuleBuilder parentBuilder;
    private final ArrayList<String> messageSequence;

    WaitForMessageBuilder(DockerRuleBuilder parentBuilder, String firstMessage) {
        this.parentBuilder = parentBuilder;
        this.messageSequence = new ArrayList<>();
        this.messageSequence.add(firstMessage);
    }

    /** Next message in sequence to wait for */
    public WaitForMessageBuilder nextMessage(String nextMessage) {
        messageSequence.add(nextMessage);
        return this;
    }

    /** End message sequence and continue with rule config. */
    public DockerRuleBuilder waitDone() {
        parentBuilder.waitForMessage(messageSequence);
        return parentBuilder;
    }
}
