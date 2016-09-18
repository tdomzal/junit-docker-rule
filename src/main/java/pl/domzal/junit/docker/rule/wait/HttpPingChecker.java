package pl.domzal.junit.docker.rule.wait;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Check whether a given URL is available
 */
public class HttpPingChecker implements StartConditionCheck {

    // Default status codes
    public static final int DEFAULT_MIN_STATUS = 200;
    public static final int DEFAULT_MAX_STATUS = 399;

    // Default HTTP Method to use
    public static final String DEFAULT_HTTP_METHOD = "HEAD";

    // Timeout for ping
    private static final int HTTP_PING_TIMEOUT = 500;

    // Disable HTTP client retries by default.
    public static final int HTTP_CLIENT_RETRIES = 0;

    private int statusMin, statusMax;
    private String url;
    private String method;

    /**
     * Ping the given URL
     *
     * @param waitUrl URL to check
     * @param method HTTP method to use
     * @param statusPattern Status code(s) to match. May be specified as single number ("302") or status range ("200..302").
     */
    public HttpPingChecker(String waitUrl, String method, String statusPattern) {
        this.url = waitUrl;
        this.method = method;

        if (method == null) {
            this.method = DEFAULT_HTTP_METHOD;
        }

        if (statusPattern == null) {
            statusMin = DEFAULT_MIN_STATUS;
            statusMax = DEFAULT_MAX_STATUS;
        } else {
            Matcher matcher = Pattern.compile("^(\\d+)\\s*\\.\\.+\\s*(\\d+)$").matcher(statusPattern);
            if (matcher.matches()) {
                statusMin = Integer.parseInt(matcher.group(1));
                statusMax = Integer.parseInt(matcher.group(2));
            } else {
                statusMin = statusMax = Integer.parseInt(statusPattern);
            }
        }
    }

    /**
     * Ping the given URL using method HTTP HEAD and accept status codes
     * from {@value #DEFAULT_MIN_STATUS} to {@value #DEFAULT_MAX_STATUS}.
     *
     * @param waitUrl URL to check
     */
    public HttpPingChecker(String waitUrl) {
        this(waitUrl, null, null);
    }

    @Override
    public boolean check() {
        try {
            return ping();
        } catch (IOException exception) {
            return false;
        }
    }

    @Override
    public String describe() {
        return String.format("http ping to '%s' with method '%s'", url, method);
    }

    private boolean ping() throws IOException {
        RequestConfig requestConfig =
                RequestConfig.custom()
                        .setSocketTimeout(HTTP_PING_TIMEOUT)
                        .setConnectTimeout(HTTP_PING_TIMEOUT)
                        .setConnectionRequestTimeout(HTTP_PING_TIMEOUT)
                        .build();
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(HTTP_CLIENT_RETRIES, false))
                .build();
        try {
            CloseableHttpResponse response = httpClient.execute(RequestBuilder.create(method.toUpperCase()).setUri(url).build());
            try {
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 501) {
                    throw new IllegalArgumentException("Invalid or not supported HTTP method '" + method.toUpperCase() + "' for checking " + url);
                }
                return (responseCode >= statusMin && responseCode <= statusMax);
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }

    @Override
    public void after() { }
}
