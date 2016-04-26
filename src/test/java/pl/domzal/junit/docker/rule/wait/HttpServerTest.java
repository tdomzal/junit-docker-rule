package pl.domzal.junit.docker.rule.wait;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.domzal.junit.docker.rule.AssertHtml;

public class HttpServerTest extends HttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServerTest.class);

    @Test
    public void shouldServeContentCheckedWithSimpleUrlConnection() throws IOException {
        // given
        serverResponse.setResponseContent("other sample content\n");
        LOG.info("url: {}", getHttpAddress());
        // when
        URL url = new URL(getHttpAddress());
        URLConnection conn = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        // then
        assertEquals("other sample content", in.readLine());
    }

    @Test
    public void shouldReturnDefaults() {
        AssertHtml.page(getHttpAddress()).returnsCode(HttpURLConnection.HTTP_OK).contentContins("").execute();
    }

    @Test
    public void shouldSetContent() {
        serverResponse.setResponseContent("sample content");
        AssertHtml.page(getHttpAddress()).returnsCode(HttpURLConnection.HTTP_OK).contentContins("sample content").execute();
    }

    @Test
    public void shouldSetErrorCode() {
        serverResponse.setErrorCode(302);
        AssertHtml.page(getHttpAddress()).returnsCode(302).execute();
    }

}
