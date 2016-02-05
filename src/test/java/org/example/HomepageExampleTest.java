package org.example;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.http.client.fluent.Request;
import org.junit.Rule;
import org.junit.Test;

import pl.domzal.junit.docker.rule.DockerRule;

public class HomepageExampleTest {

    @Rule
    public DockerRule container = DockerRule.builder() //
            .imageName("nginx") //
            .build();

    @Test
    public void shouldExposePorts() throws InterruptedException, IOException {

        // url container homepage will be exposed under
        String homepage = "http://"+container.getDockerHost()+":"+container.getExposedContainerPort("80")+"/";

        // lest use fluent apache http client to simply retrieve homepage content
        String pageContent = Request.Get(homepage).connectTimeout(1000).socketTimeout(1000).execute().returnContent().asString();

        // make sure this is indeed nginx welcome page
        assertThat(pageContent, containsString("Welcome to nginx!"));
    }

}
