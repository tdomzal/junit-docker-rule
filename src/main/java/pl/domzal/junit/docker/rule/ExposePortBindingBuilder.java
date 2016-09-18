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

import pl.domzal.junit.docker.rule.ex.InvalidPortDefinition;

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
        addBinding(Ports.portWithProtocol(containerPort), PortBinding.of(BIND_ALL, hostPort));
        return this;
    }

    public ExposePortBindingBuilder expose(String containerPort) {
        assertValidContainerPort(containerPort);
        addBinding(Ports.portWithProtocol(containerPort), PortBinding.randomPort(BIND_ALL));
        return this;
    }

    private void addBinding(String containerBind, PortBinding hostBind) {
        if (bindings.containsKey(containerBind)) {
            bindings.get(containerBind).add(hostBind);
        } else {
            bindings.put(containerBind, Lists.newArrayList(hostBind));
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
        if (Ports.isPortWithProtocol(portToCheck)) {
            // ok
        } else if (StringUtils.isNumeric(portToCheck)) {
            // ok
        } else {
            throw new InvalidPortDefinition(portToCheck);
        }
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
