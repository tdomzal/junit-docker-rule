package pl.domzal.junit.docker.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

/**
 * Builder for {@link Map} required by {@link HostConfig.Builder#portBindings(Map)}.
 */
class ExposePortBindingBuilder {

    static final String BIND_ALL = "0.0.0.0";

    Map<String, List<PortBinding>> bindings = new HashMap<>();

    static ExposePortBindingBuilder builder() {
        return new ExposePortBindingBuilder();
    }

    public ExposePortBindingBuilder expose(String hostPort, String containerPort) {
        assertIsNumber(hostPort);
        assertValidContainerPort(containerPort);
        assertHostPortNotAssigned(hostPort);

        String containerBind = containerBindWithProtocol(containerPort);
        PortBinding hostBind = PortBinding.of(BIND_ALL, hostPort);
        if (bindings.containsKey(containerBind)) {
            bindings.get(containerBind).add(hostBind);
        } else {
            bindings.put(containerBind, Lists.newArrayList(hostBind));
        }

        return this;
    }

    private String containerBindWithProtocol(String containerPort) {
        if (isPortWithProtocol(containerPort)) {
            return containerPort;
        } else if (StringUtils.isNumeric(containerPort)) {
            return containerPort + "/tcp";
        } else {
            throw new InvalidPortDefinition(containerPort);
        }

    }

    Map<String, List<PortBinding>> hostBindings() {
        return bindings;
    }

    Set<String> containerExposedPorts() {
        return bindings.keySet();
    }

    private void assertIsNumber(String portToCheck) {
        if ( ! StringUtils.isNumeric(portToCheck) ) {
            throw new InvalidPortDefinition(portToCheck);
        }
    }

    private void assertValidContainerPort(String portToCheck) {
        if (isPortWithProtocol(portToCheck)) {
            // ok
        } else if (StringUtils.isNumeric(portToCheck)) {
            // ok
        } else {
            throw new InvalidPortDefinition(portToCheck);
        }
    }

    private boolean isPortWithProtocol(String portToCheck) {
        if (StringUtils.length(portToCheck) > 4 && (StringUtils.endsWith(portToCheck, "/tcp") || StringUtils.endsWith(portToCheck, "/udp"))) {
            String port = StringUtils.left(portToCheck, portToCheck.length() - 4);
            return StringUtils.isNumeric(port);
        }
        return false;
    }

    private void assertHostPortNotAssigned(String hostPort) {
        Set<Entry<String, List<PortBinding>>> bindingsEntries = bindings.entrySet();
        for (Entry<String, List<PortBinding>> bindingPair : bindingsEntries) {
            List<PortBinding> hostBindings = bindingPair.getValue();
            for (PortBinding hostBinding : hostBindings) {
                if (hostBinding.hostPort().equals(hostPort)) {
                    throw new IllegalStateException(String.format("host port %s is already assigned for binding %s:%s", hostPort, hostPort, bindingPair.getKey()));
                }
            }
        }
    }

}
