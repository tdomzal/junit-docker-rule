package pl.domzal.junit.docker.rule;

/**
 * Invalid link definition.
 */
public class InvalidParameter extends IllegalStateException {

    public InvalidParameter(String message) {
        super(message);
    }

}
