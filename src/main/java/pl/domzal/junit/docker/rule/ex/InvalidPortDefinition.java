package pl.domzal.junit.docker.rule.ex;

public class InvalidPortDefinition extends IllegalStateException {

    public InvalidPortDefinition(String port) {
        super("Invalid port: "+port);
    }

}
