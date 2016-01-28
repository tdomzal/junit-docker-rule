package pl.domzal.junit.docker.rule;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.spotify.docker.client.messages.PortBinding;

@Category(test.category.Stable.class)
public class ExposePortBindingBuilderTest {

    private static String BIND_ALL = ExposePortBindingBuilder.BIND_ALL;

    ExposePortBindingBuilder builder = ExposePortBindingBuilder.builder();

    @Test
    public void shouldExposeSingleTcpPortWithNoProtocolSpecifed() throws Exception {
        //when
        builder.expose("8080", "80");
        Set<String> containerExposedPorts = builder.containerExposedPorts();
        Map<String, List<PortBinding>> hostBindings = builder.hostBindings();

        //then
        assertEquals(1, containerExposedPorts.size());
        assertTrue(containerExposedPorts.contains("80/tcp"));
        assertEquals(1, hostBindings.size());
        assertTrue(hostBindings.containsKey("80/tcp"));
        List<PortBinding> binds80 = hostBindings.get("80/tcp");
        assertEquals(1, binds80.size());
        PortBinding portBinding = binds80.get(0);
        assertEquals(PortBinding.of(BIND_ALL, 8080), portBinding);
    }

    @Test
    public void shouldExposeSingleTcpPortWithProtocolSpecified() throws Exception {
        //when
        builder.expose("8080", "80/tcp");
        Set<String> containerExposedPorts = builder.containerExposedPorts();
        Map<String, List<PortBinding>> hostBindings = builder.hostBindings();

        //then
        assertEquals(1, containerExposedPorts.size());
        assertTrue(containerExposedPorts.contains("80/tcp"));
        assertEquals(1, hostBindings.size());
        assertTrue(hostBindings.containsKey("80/tcp"));
        List<PortBinding> binds80 = hostBindings.get("80/tcp");
        assertEquals(1, binds80.size());
        PortBinding portBinding = binds80.get(0);
        assertEquals(PortBinding.of(BIND_ALL, 8080), portBinding);
    }

    @Test
    public void shouldExposeSingleUdpPort() throws Exception {
        //when
        builder.expose("8080", "80/udp");
        Set<String> containerExposedPorts = builder.containerExposedPorts();
        Map<String, List<PortBinding>> hostBindings = builder.hostBindings();

        //then
        assertEquals(1, containerExposedPorts.size());
        assertTrue(containerExposedPorts.contains("80/udp"));
        assertEquals(1, hostBindings.size());
        assertTrue(hostBindings.containsKey("80/udp"));
        List<PortBinding> binds80 = hostBindings.get("80/udp");
        assertEquals(1, binds80.size());
        PortBinding portBinding = binds80.get(0);
        assertEquals(PortBinding.of(BIND_ALL, 8080), portBinding);
    }

    @Test
    public void shouldExposeTwoTcpPorts() {
        //when
        builder.expose("8181", "80").expose("7171", "70");
        Set<String> containerExposedPorts = builder.containerExposedPorts();
        Map<String, List<PortBinding>> hostBindings = builder.hostBindings();

        //then
        assertEquals(2, containerExposedPorts.size());
        assertTrue(containerExposedPorts.contains("70/tcp"));
        assertTrue(containerExposedPorts.contains("80/tcp"));
        assertEquals(2, hostBindings.size());
        assertTrue(hostBindings.containsKey("70/tcp"));
        assertTrue(hostBindings.containsKey("80/tcp"));
        // 70 bind
        List<PortBinding> binds70 = hostBindings.get("70/tcp");
        assertEquals(1, binds70.size());
        assertEquals(PortBinding.of(BIND_ALL, 7171), binds70.get(0));
        // 80 bind
        List<PortBinding> binds80 = hostBindings.get("80/tcp");
        assertEquals(1, binds80.size());
        assertEquals(PortBinding.of(BIND_ALL, 8181), binds80.get(0));
    }

    @Test
    public void shouldExposeTwoMixedProtocolPorts() {
        //when
        builder.expose("8181", "80").expose("7171", "80/udp");
        Set<String> containerExposedPorts = builder.containerExposedPorts();
        Map<String, List<PortBinding>> hostBindings = builder.hostBindings();

        //then
        assertEquals(2, containerExposedPorts.size());
        assertTrue(containerExposedPorts.contains("80/tcp"));
        assertTrue(containerExposedPorts.contains("80/udp"));
        assertEquals(2, hostBindings.size());
        assertTrue(hostBindings.containsKey("80/udp"));
        assertTrue(hostBindings.containsKey("80/tcp"));
        // 70 bind
        List<PortBinding> binds70 = hostBindings.get("80/udp");
        assertEquals(1, binds70.size());
        assertEquals(PortBinding.of(BIND_ALL, 7171), binds70.get(0));
        // 80 bind
        List<PortBinding> binds80 = hostBindings.get("80/tcp");
        assertEquals(1, binds80.size());
        assertEquals(PortBinding.of(BIND_ALL, 8181), binds80.get(0));
    }

    @Test
    public void shouldMergeTwoTcpPorts() {
        //when
        builder.expose("8181", "80").expose("7171", "80");
        Set<String> containerExposedPorts = builder.containerExposedPorts();
        Map<String, List<PortBinding>> hostBindings = builder.hostBindings();

        //then
        assertEquals(1, containerExposedPorts.size());
        assertTrue(containerExposedPorts.contains("80/tcp"));
        assertEquals(1, hostBindings.size());
        assertTrue(hostBindings.containsKey("80/tcp"));
        // 80 bind
        List<PortBinding> binds80 = hostBindings.get("80/tcp");
        assertEquals(2, binds80.size());
        assertEquals(PortBinding.of(BIND_ALL, 8181), binds80.get(0));
        assertEquals(PortBinding.of(BIND_ALL, 7171), binds80.get(1));
    }

    @Test(expected=IllegalStateException.class)
    public void shouldFailOnHostPortExposeDuplicated() {
        builder.expose("8080", "70").expose("8080", "70");
    }

    public void shouldNotFailWhenExposedSamePortDifferentProtocols() {
        builder.expose("8080", "70").expose("8080", "70/udp");
    }

    @Test(expected=InvalidPortDefinition.class)
    public void shouldDropNotSupportedPortRanges() {
        builder.expose("1-2", "3-4");
    }
}
