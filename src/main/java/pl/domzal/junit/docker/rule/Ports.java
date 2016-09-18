package pl.domzal.junit.docker.rule;

import org.apache.commons.lang3.StringUtils;

import pl.domzal.junit.docker.rule.ex.InvalidPortDefinition;

class Ports {

    public static final String TCP_PORT_SUFFIX = "/tcp";
    public static final String UDP_PORT_SUFFIX = "/udp";
    public static final int PORT_SUFFIX_LENGTH = 4;

    /**
     * Prepare port definition ready for binding.<p/>
     * Example (input -&gt; ouput):
     * <pre>
     * "80" -> "80/tcp"
     * "80/tcp" -> "80/tcp"
     * "80/udp" -> "80/udp"
     * </pre>
     */
    public static String portWithProtocol(String port) {
        if (isPortWithProtocol(port)) {
            return port;
        } else if (StringUtils.isNumeric(port)) {
            return port + "/tcp";
        } else {
            throw new InvalidPortDefinition(port);
        }

    }

    /**
     *
     * @return <code>true</code> if given value matching format 'number'/tcp or 'number'/udp
     */
    public static boolean isPortWithProtocol(String portToCheck) {
        if (StringUtils.length(portToCheck) > PORT_SUFFIX_LENGTH && (StringUtils.endsWith(portToCheck, TCP_PORT_SUFFIX) || StringUtils.endsWith(portToCheck, UDP_PORT_SUFFIX))) {
            String port = StringUtils.left(portToCheck, portToCheck.length() - PORT_SUFFIX_LENGTH);
            return StringUtils.isNumeric(port);
        }
        return false;
    }

}
