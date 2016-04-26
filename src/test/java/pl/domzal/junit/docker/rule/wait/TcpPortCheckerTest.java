package pl.domzal.junit.docker.rule.wait;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class TcpPortCheckerTest {

    @Test
    public void shouldCheckPort() throws Exception {
        HttpServer server = new HttpServer();
        server.start();
        try {
            TcpPortChecker testee = new TcpPortChecker(server.getServerAddress(), Arrays.asList(new Integer(server.getPort())));
            assertTrue(testee.check());
        } finally {
            server.stop();
        }
    }

    @Test
    public void shouldFailWhenPortNotOpen() {
        TcpPortChecker testee = new TcpPortChecker("localhost", Arrays.asList(new Integer(11111)));
        assertFalse(testee.check());
    }
}