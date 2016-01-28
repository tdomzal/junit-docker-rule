package pl.domzal.junit.docker.rule;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.spotify.docker.client.messages.PortBinding;

public class ExposePortBindingBuilderTest {

    private static String BIND_ALL = ExposePortBindingBuilder.BIND_ALL;

    ExposePortBindingBuilder builder = ExposePortBindingBuilder.builder();

    @Test
    public void shouldExposeSingleFixedPort() throws Exception {
        //when
        builder.expose("8080", "80");
        Map<String, List<PortBinding>> build = builder.hostBindings();

        //then
        assertEquals(1, build.size());
        assertTrue(build.containsKey("80/tcp"));
        List<PortBinding> binds80 = build.get("80/tcp");
        assertEquals(1, binds80.size());
        PortBinding portBinding = binds80.get(0);
        assertEquals(PortBinding.of(BIND_ALL, 8080), portBinding);
    }

    @Test
    public void shouldExposeTwoDifferentPorts() {
        //when
        builder.expose("8181", "80").expose("7171", "70");
        Map<String, List<PortBinding>> build = builder.hostBindings();

        //then
        assertEquals(2, build.size());
        assertTrue(build.containsKey("70/tcp"));
        assertTrue(build.containsKey("80/tcp"));
        // 70 bind
        List<PortBinding> binds70 = build.get("70/tcp");
        assertEquals(1, binds70.size());
        assertEquals(PortBinding.of(BIND_ALL, 7171), binds70.get(0));
        // 80 bind
        List<PortBinding> binds80 = build.get("80/tcp");
        assertEquals(1, binds80.size());
        assertEquals(PortBinding.of(BIND_ALL, 8181), binds80.get(0));
    }

    @Test
    public void shouldMergeSameContainerPort() {
        //when
        builder.expose("8181", "80").expose("7171", "80");
        Map<String, List<PortBinding>> build = builder.hostBindings();

        //then
        assertEquals(1, build.size());
        assertTrue(build.containsKey("80/tcp"));
        // 80 bind
        List<PortBinding> binds80 = build.get("80/tcp");
        assertEquals(2, binds80.size());
        assertEquals(PortBinding.of(BIND_ALL, 8181), binds80.get(0));
        assertEquals(PortBinding.of(BIND_ALL, 7171), binds80.get(1));
    }

    @Test(expected=IllegalStateException.class)
    public void shouldFailWhenHostPortExposeDuplicated() {
        builder.expose("8080", "70").expose("8080", "70");
    }

    @Test(expected=IllegalStateException.class)
    public void shouldDropNotSupportedPortRanges() {
        builder.expose("1-2", "3-4");
    }
}
