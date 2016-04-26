package pl.domzal.junit.docker.rule.wait;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Check whether a given TCP port is available
 */
public class TcpPortChecker implements WaitChecker {

    // Timeout for ping
    private static final int TCP_PING_TIMEOUT = 500;

    private final String host;
    private final List<Integer> ports;

    private final List<InetSocketAddress> pending;

    public TcpPortChecker(String host, List<Integer> ports) {
        this.host = host;
        this.ports = ports;

        this.pending = new ArrayList<>();
        for (int port : ports) {
            this.pending.add(new InetSocketAddress(host, port));
        }

    }

    public List<Integer> getPorts() {
        return ports;
    }

    public List<InetSocketAddress> getPending() {
        return pending;
    }

    @Override
    public boolean check() {
        Iterator<InetSocketAddress> iter = pending.iterator();

        while (iter.hasNext()) {
            InetSocketAddress address = iter.next();

            try {
                Socket s = new Socket();
                s.connect(address, TCP_PING_TIMEOUT);
                s.close();
                iter.remove();
            } catch (IOException e) {
                // Ports isn't opened, yet. So don't remove from queue.

            }

        }
        return pending.isEmpty();
    }

    @Override
    public String describe() {
        return String.format("tcp port check '%s:%s'", host, Arrays.asList(ports));
    }

    @Override
    public void after() { }
}
