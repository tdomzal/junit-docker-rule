package pl.domzal.junit.docker.rule.ex;

/**
 * Port is not exposed and cannot be used in wait for conditions.
 */
public class PortNotExposedException extends IllegalStateException {

    public PortNotExposedException(String port) {
        super(port);
    }

}
