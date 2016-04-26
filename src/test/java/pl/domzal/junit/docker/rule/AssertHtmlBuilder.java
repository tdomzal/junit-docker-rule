package pl.domzal.junit.docker.rule;

import java.io.IOException;

public interface AssertHtmlBuilder {

    /**
     * Returned response should have given http error code.
     */
    AssertHtmlBuilder returnsCode(int httpErrorCode);

    /**
     * Returned conent should contain given string.
     */
    AssertHtmlBuilder contentContins(String contentFragment);

    /**
     * Execute html assert with given settings.
     */
    void execute();

}
