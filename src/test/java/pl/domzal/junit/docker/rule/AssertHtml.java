package pl.domzal.junit.docker.rule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

@Category(test.category.Stable.class)
public class AssertHtml {

    private static Logger log = LoggerFactory.getLogger(AssertHtml.class);

    private static final int NO_OF_LINES_TO_LOG = 5;
    private static final int NO_OF_RETRIES = 3;
    private static final int RETRY_WAIT_TIME = 3000;

    /**
     * Check if page contains specified string. If no page is yet available - retry few times.
     * @param remotePageUrl Page URL
     * @param containsString String to look for
     *
     * @return <code>true</code> When page was successfully retreived and contains specified string.
     */
    public static boolean pageContainsString(String remotePageUrl, String containsString) {
        String pageContent = retryGet(new ContentGetter(remotePageUrl), NO_OF_RETRIES, RETRY_WAIT_TIME);
        log.debug("\n" + head(pageContent, NO_OF_LINES_TO_LOG));
        return pageContent.contains(containsString);
    }

    /**
     * Check if page contains specified string. Try to retrieve page content only once.
     * @param remotePageUrl Page URL
     * @param containsString String to look for
     *
     * @return <code>true</code> When page was successfully retreived and contains specified string.
     */
    public static boolean pageContainsStringNoRetry(String remotePageUrl, String containsString) {
        String pageContent = retryGet(new ContentGetter(remotePageUrl), 1, 0);
        log.debug("\n" + head(pageContent, NO_OF_LINES_TO_LOG));
        return pageContent.contains(containsString);
    }

    /**
     * Check if page contains specified string.
     * @param remotePageUrl Page URL
     * @param containsString String to look for
     * @param noOfTries No of tries (1 - single try, 2 - two.. etc.).
     * @param retryWaitTimeMs Time between retries. Not uses for single try.
     *
     * @return <code>true</code> When page was successfully retreived and contains specified string.
     */
    static boolean pageContainsString(String remotePageUrl, String containsString, int noOfTries, int retryWaitTimeMs) {
        String pageContent = retryGet(new ContentGetter(remotePageUrl), noOfTries, retryWaitTimeMs);
        log.debug("\n" + head(pageContent, NO_OF_LINES_TO_LOG));
        return pageContent.contains(containsString);
    }

    private static class ContentGetter implements Getter<String> {
        private final String remotePageUrl;

        public ContentGetter(String remotePageUrl) {
            this.remotePageUrl = remotePageUrl;
        }

        @Override
        public String get() throws Exception {
            return Request.Get(remotePageUrl).connectTimeout(1000).socketTimeout(1000).execute().returnContent().asString();
        }
    }

    private static class ResponseGetter implements Getter<Response> {
        private final String remotePageUrl;

        public ResponseGetter(String remotePageUrl) {
            this.remotePageUrl = remotePageUrl;
        }

        @Override
        public Response get() throws Exception {
            return Request.Get(remotePageUrl).connectTimeout(1000).socketTimeout(1000).execute();
        }
    }

    public static AssertHtmlBuilder page(String remotePageUrl) {
        return new AssertHtmlBuilderImpl(remotePageUrl);
    }

    private static class AssertHtmlBuilderImpl implements AssertHtmlBuilder {

        private final String remotePageUrl;
        private int httpErrorCode = 200;
        private String contentFragment;

        public AssertHtmlBuilderImpl(String remotePageUrl) {
            this.remotePageUrl = remotePageUrl;
        }

        @Override
        public AssertHtmlBuilder returnsCode(int httpErrorCode) {
            this.httpErrorCode = httpErrorCode;
            return this;
        }

        @Override
        public AssertHtmlBuilder contentContins(String contentFragment) {
            this.contentFragment = contentFragment;
            return this;
        }

        @Override
        public void execute() {
            try {
                Response response = retryGet(new ResponseGetter(remotePageUrl), NO_OF_RETRIES, RETRY_WAIT_TIME);
                HttpResponse httpResponse = response.returnResponse();
                assertEquals("http error code does not match", httpErrorCode, httpResponse.getStatusLine().getStatusCode());
                if (contentFragment != null) {
                    HttpEntity entity = httpResponse.getEntity();
                    if (entity == null) {
                        throw new IllegalStateException("Response contains no content");
                    }
                    try (InputStream is = entity.getContent()) {
                        byte[] bytes = IOUtils.toByteArray(is);
                        String pageContent = new String(bytes, Charset.defaultCharset());
                        assertNotNull("page content is empty", pageContent);
                        log.debug("\n" + head(pageContent, NO_OF_LINES_TO_LOG));
                        assertTrue(String.format("page content '%s' does not contain '%s'", pageContent, contentFragment), pageContent.contains(contentFragment));
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private interface Getter<T> {
        T get() throws Exception;
    }

    private static <T> T retryGet(Getter<T> pageGetter, int noOfRetries, long retryWaitTime) throws IllegalStateException {
        int retry = 1;
        try {
            Throwable lastException = null;
            do {
                try {
                    return pageGetter.get();
                } catch (Exception e) {
                    retry++;
                    lastException = e;
                    log.warn("get failded: " + e.getMessage());
                    log.info("pausing for " + retryWaitTime + " ms ...");
                    Thread.sleep(retryWaitTime);
                }
            } while (retry <= noOfRetries);
            throw new IllegalStateException(String.format("Fail %s times, last reason:", noOfRetries), lastException);
        } catch (InterruptedException ie) {
            throw new IllegalStateException("Interrupted while waiting for next retry", ie);
        }
    }

    private static String head(String pageContent, int noOfLines) {
        String[] allLines = StringUtils.split(pageContent, '\n');
        String[] headLines = ArrayUtils.subarray(allLines, 0, noOfLines);
        return StringUtils.join(headLines, '\n');
    }

    @Test
    public void shouldHeadNoOfLines() {
        // given
        String input = "one\ntwo\nthree\\four";
        String expected2 = "one\ntwo";
        // when, then - different cases
        assertEquals("", head(input, 0));
        assertEquals(expected2, head(input, 2));
        assertEquals(input, head(input, 4));
        assertEquals(input, head(input, 6));
    }

    @Test
    public void shouldRetryAndFail() throws Exception {
        // given
        Getter<String> testGetter = (Getter<String>) mock(Getter.class);
        when(testGetter.get()).thenThrow(new Exception("kaboom"));
        // when
        try {
            retryGet(testGetter, 3, 10);
        } catch (IllegalStateException e) {
            //expected
            log.info(e.getMessage(), e);
        }
        verify(testGetter, times(3)).get();
    }

    @Test
    public void shouldRetryAndSuccess() throws Exception {
        // given
        Getter<String> testGetter = (Getter<String>) mock(Getter.class);
        when(testGetter.get()).thenThrow(new Exception("kaboom1")).thenThrow(new Exception("kaboom2")).thenReturn("value");
        // when
        String value = retryGet(testGetter, 3, 10);
        // then
        verify(testGetter, times(3)).get();
        assertEquals("value", value);
    }
}
