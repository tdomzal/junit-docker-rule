package pl.domzal.junit.docker.rule;

public class InvalidPortDefinition extends IllegalStateException {

    public InvalidPortDefinition(String port) {
        super("Invalid port: "+port);
    }

}
