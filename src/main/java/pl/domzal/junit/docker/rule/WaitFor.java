package pl.domzal.junit.docker.rule;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import pl.domzal.junit.docker.rule.wait.HttpPingChecker;
import pl.domzal.junit.docker.rule.wait.LogChecker;
import pl.domzal.junit.docker.rule.wait.LogSequenceChecker;
import pl.domzal.junit.docker.rule.wait.TcpPortChecker;
import pl.domzal.junit.docker.rule.wait.StartConditionCheck;
import pl.domzal.junit.docker.rule.wait.StartCondition;

/**
 * Predefined set of handy {@link StartConditionCheck} builders.
 */
public class WaitFor {

    private static final Logger log = LoggerFactory.getLogger(WaitFor.class);

    /**
     * Wait for specified text in log on container start.
     * Whole log content (from container start) is checked so this condition
     * will work independent of any other wait conditions.
     * Rule startup will fail when message will not be found.
     *
     * @param logMessage Message to wait for.
     */
    public static StartCondition logMessage(final String logMessage) {
        return new StartCondition() {
            @Override
            public StartConditionCheck build(DockerRule currentRule) {
                log.debug("new wait for condition - message: '{}'", logMessage);
                return new LogChecker(currentRule, logMessage);
            }
        };
    }

    /**
     * Wait for message sequence in log container start.
     *
     * @param message
     */
    public static StartCondition logMessageSequence(String... message) {
        return logMessageSequence(Lists.newArrayList(message));
    }

    public static StartCondition logMessageSequence(final List<String> messageSequence) {
        return new StartCondition() {
            @Override
            public StartConditionCheck build(DockerRule currentRule) {
                log.debug("new wait for condition - message sequence: '{}'", messageSequence);
                return new LogSequenceChecker(messageSequence);
            }
        };
    }

    /**
     * Wait for TCP port listening under given internal container port.
     * Given port MUST be exposed (with {@link DockerRuleBuilder#expose(String, String)} or
     * {@link DockerRuleBuilder#publishAllPorts(boolean)}) (reachable from the test
     * code point of view).
     * <p>
     * Side note:
     * Internal port is required for convenience - rule will find matching external port
     * or, report error at startup when given internal port was not exposed.
     * <p>
     * Side note 2:
     * <b>TCP port check depends of docker internal port-forwarding feature and docker server setup</b>.
     * In short: won't work if docker engine forwards port using <i>docker-proxy</i> (aka <i>userland proxy</i>)
     * - will report port opening almost instantly and NOT wait for underlying port opening.
     * To make things worst - <b>this is default configuration on some platforms so it is better to not
     * rely on this method at all</b>.
     * <p>
     * Additional <i>userland proxy</i> info:<ul>
     *     <li><a href="https://docs.docker.com/engine/userguide/networking/default_network/binding/">Docker docs / Bind container ports to the host</a></li>
     *     <li><a href="https://docs.docker.com/engine/reference/commandline/dockerd/">Docker docs / daemon options</a></li>
     *     <li><a href="https://github.com/docker/docker/issues/8356">Issue / Make it possible to disable userland proxy</a></li>
     * </ul>
     *
     * @param internalTcpPorts TCP port (or ports) to scan (internal, MUST be exposed for wait to work).
     */
    public static StartCondition tcpPort(final int... internalTcpPorts) {
        return tcpPort(Ints.asList(internalTcpPorts));
    }

    private static StartCondition tcpPort(final List<Integer> internalPorts) {
        return new StartCondition() {
            @Override
            public StartConditionCheck build(DockerRule currentRule) {
                List<Integer> externalPorts = Lists.newArrayList();
                for (Integer intPort : internalPorts) {
                    externalPorts.add(currentRule.findExternalPort(intPort));
                }
                log.debug("new wait for condition - tcp port(s) open: {} (external port(s): {})", internalPorts, externalPorts);
                return new TcpPortChecker(currentRule.getDockerHost(), externalPorts);
            }
        };
    }

    /**
     * Wait for http endpoint availability under given <b>internal</b> container port.
     * Given port MUST be exposed (with {@link DockerRuleBuilder#expose(String, String)} or
     * {@link DockerRuleBuilder#publishAllPorts(boolean)}) (reachable from the test
     * code point of view).
     * <p>
     * Side note: Internal port is required for convenience - rule will find matching
     * external port or, report error at startup when given internal port was not exposed.
     *
     * @param internalHttpPort Http port to scan for availability. Port is scanned with HTTP HEAD method
     *                 until response with error code 2xx or 3xx is returned or until timeout.
     *                 Port MUST be exposed for wait to work and given port number must
     *                 be internal (as seen on container, not as on host) port number.
     */
    public static StartCondition httpPing(final int internalHttpPort) {
        return new StartCondition() {
            @Override
            public StartConditionCheck build(DockerRule currentRule) {
                String exposedPort = currentRule.getExposedContainerPort(Integer.toString(internalHttpPort));
                String pingUrl = String.format("http://%s:%s/", currentRule.getDockerHost(), exposedPort);
                log.debug("new wait for condition - http ping port: {}, url: '{}'", internalHttpPort, pingUrl);
                return new HttpPingChecker(pingUrl, null, null);
            }
        };
    }

}
