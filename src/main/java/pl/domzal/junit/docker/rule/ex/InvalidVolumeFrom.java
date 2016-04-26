package pl.domzal.junit.docker.rule.ex;

/**
 * Docker volume mount from path is invalid in given context.
 */
public class InvalidVolumeFrom extends IllegalStateException {

    public InvalidVolumeFrom(String message) {
        super(message);
    }

}
