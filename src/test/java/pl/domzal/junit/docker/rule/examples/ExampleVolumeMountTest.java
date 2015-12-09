package pl.domzal.junit.docker.rule.examples;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import pl.domzal.junit.docker.rule.DockerRule;

public class ExampleVolumeMountTest {

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void setup() throws IOException {
        File testFile = tempFolder.newFile("somefile");
        FileUtils.write(testFile, "1234567890");
    }

    @Rule
    public DockerRule testee = DockerRule.builder()//
            .imageName("busybox")//
            .mountFrom(tempFolder.getRoot()).to("/somedir", "ro")//
            .cmd("sh", "-c", "cat /somedir/somefile")//
            .build();

    @Test(timeout = 10000)
    public void shouldReadMountFromJavaFile() throws Throwable {
        testee.waitFor("1234567890", 10);
    }

}
