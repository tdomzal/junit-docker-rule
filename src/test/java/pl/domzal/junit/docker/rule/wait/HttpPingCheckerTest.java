package pl.domzal.junit.docker.rule.wait;

import static org.junit.Assert.*;

import org.junit.Test;

public class HttpPingCheckerTest extends HttpServer {

    @Test
    public void shouldSuccessOnHead() throws Exception {
        HttpPingChecker testee = new HttpPingChecker(getHttpAddress());
        assertTrue(testee.check());
    }

    @Test
    public void shouldSuccessOnGet() throws Exception {
        HttpPingChecker testee = new HttpPingChecker(getHttpAddress(), "GET", "200");
        assertTrue(testee.check());
    }

    @Test
    public void shouldAcceptStatusRange() throws Exception {
        HttpPingChecker testee = new HttpPingChecker(getHttpAddress(), "GET", "200..201");
        serverResponse.setErrorCode(200);
        assertTrue(testee.check());
        serverResponse.setErrorCode(201);
        assertTrue(testee.check());
        serverResponse.setErrorCode(202);
        assertFalse(testee.check());
    }

    @Test
    public void shouldSuccessOnErrorCode201() {
        serverResponse.setErrorCode(201);
        HttpPingChecker testee = new HttpPingChecker(getHttpAddress());
        assertTrue(testee.check());
    }

    @Test
    public void shouldSuccessOnErrorCode399() {
        serverResponse.setErrorCode(399);
        HttpPingChecker testee = new HttpPingChecker(getHttpAddress());
        assertTrue(testee.check());
    }

    @Test
    public void shouldFailCheckOnErrorCode500() {
        serverResponse.setErrorCode(500);
        HttpPingChecker testee = new HttpPingChecker(getHttpAddress());
        assertFalse("should fail on error code 500", testee.check());
    }

}