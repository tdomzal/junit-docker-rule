package pl.domzal.junit.docker.rule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(test.category.Stable.class)
public class AssertHtml {

    private static Logger log = LoggerFactory.getLogger(AssertHtml.class);

    private static final int NO_OF_LINES_TO_LOG = 5;
    private static final int NO_OF_RETRIES = 3;
    private static final int RETRY_WAIT_TIME = 3000;

    public static boolean pageContainsString(String remotePageUrl, String containsString) {
        String pageContent = retryGet(new PageGetter(remotePageUrl), NO_OF_RETRIES, RETRY_WAIT_TIME);
        log.debug("\n"+ head(pageContent, NO_OF_LINES_TO_LOG));
        return pageContent.contains(containsString);
    }

    private static class PageGetter implements Getter<String> {
        private final String remotePageUrl;

        public PageGetter(String remotePageUrl) {
            this.remotePageUrl = remotePageUrl;
        }
        @Override
        public String get() throws Exception {
            return Request.Get(remotePageUrl).connectTimeout(1000).socketTimeout(1000).execute().returnContent().asString();
        }
    }

    private interface Getter <T> {
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
