package pl.domzal.junit.docker.rule;

public class ImagePullException extends IllegalStateException {

    public ImagePullException(String message, Throwable cause) {
        super(message, cause);
    }

}
