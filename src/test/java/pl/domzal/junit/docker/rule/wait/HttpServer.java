package pl.domzal.junit.docker.rule.wait;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);

    private com.sun.net.httpserver.HttpServer httpServer;

    private String serverAddress = "localhost";

    private int port;

    private String httpAddress;

    /**
     * Allows to set served response code and content.
     */
    protected final ServerResponse serverResponse = new ServerResponse();

    @Before
    public void start() throws IOException {
        final String path = "/1234.xml";

        // create the HttpServer
        InetSocketAddress address = new InetSocketAddress(serverAddress, 0);
        httpServer = com.sun.net.httpserver.HttpServer.create(address, 0);
        // create and register our handler
        HttpHandler handler = new ConfigurableHttpHandler(serverResponse);
        httpServer.createContext(path, handler);

        // start the server
        httpServer.start();
        port = httpServer.getAddress().getPort();
        LOG.debug("started http server {}:{} with handler {}", serverAddress, port, handler);

        httpAddress = String.format("http://%s:%d/1234.xml", serverAddress, port);
        LOG.debug("test url is: {}", httpAddress);
    }

    /**
     * Address that client should be used for test connection.
     */
    public String getHttpAddress() {
        return httpAddress;
    }

    /**
     * Address server listens on.
     */
    public int getPort() {
        return port;
    }

    /**
     * Server port listens on.
     */
    public String getServerAddress() {
        return serverAddress;
    }

    private class ConfigurableHttpHandler implements HttpHandler {

        private final ServerResponse serverResponse;

        public ConfigurableHttpHandler(ServerResponse serverResponse) {
            this.serverResponse = serverResponse;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] responseBytes = serverResponse.responseContent.getBytes();
            exchange.sendResponseHeaders(serverResponse.errorCode, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.close();
        }
    }

    @After
    public void stop() {
        // stop the server
        httpServer.stop(0);
        LOG.debug("stopped server for url: {}", httpAddress);
    }

    class ServerResponse {

        // default error code OK
        private int errorCode = HttpURLConnection.HTTP_OK;
        // default content empty
        private String responseContent = "";

        ServerResponse setErrorCode(int errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        ServerResponse setResponseContent(String responseContent) {
            this.responseContent = responseContent;
            return this;
        }
    }

}
