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
        assertIsNumber(containerPort);
        assertHostPortNotAssigned(hostPort);

        String containerBind = containerPort+"/tcp";
        PortBinding hostBind = PortBinding.of(BIND_ALL, hostPort);
        if (bindings.containsKey(containerBind)) {
            bindings.get(containerBind).add(hostBind);
        } else {
            bindings.put(containerBind, Lists.newArrayList(hostBind));
        }

        return this;
    }

    public Map<String, List<PortBinding>> build() {
        return bindings;
    }

    private void assertIsNumber(String portToCheck) {
        if ( ! StringUtils.containsOnly(portToCheck, "0123456789") ) {
            throw new IllegalStateException("port ranges not allowed: "+portToCheck);
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
