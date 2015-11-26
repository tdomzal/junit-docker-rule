package pl.domzal.junit.docker.rule;

/**
 * Docker image pull failure.
 */
public class ImagePullException extends IllegalStateException {

    public ImagePullException(String message, Throwable cause) {
        super(message, cause);
    }

}
