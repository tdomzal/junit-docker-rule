package pl.domzal.junit.docker.rule;

import java.io.IOException;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssertHtml {

    private static Logger log = LoggerFactory.getLogger(AssertHtml.class);

    public static boolean pageContainsString(String remotePageUrl, String containsString) {
        try {
            Content content = Request.Get(remotePageUrl).execute().returnContent();
            String pageContent = content.asString();
            log.debug("\n"+pageContent);
            return pageContent.contains(containsString);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
