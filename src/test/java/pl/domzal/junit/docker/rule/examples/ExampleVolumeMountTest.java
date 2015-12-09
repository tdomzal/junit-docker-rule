package pl.domzal.junit.docker.rule.examples;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.domzal.junit.docker.rule.DockerRule;

public class ExampleVolumeMountTest {

    private static Logger log = LoggerFactory.getLogger(ExampleVolumeMountTest.class);

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();

    private static File testFile;
    private static String testFilename;
    private static File testDir;

    @BeforeClass
    public static void setup() throws IOException {

        testFile = tempFolder.newFile();
        testFilename = testFile.getName();
        FileUtils.write(testFile, "1234567890");
        testDir = tempFolder.getRoot();
        log.debug("testDir: {}", testDir.getAbsolutePath());
    }

    @Rule
    public DockerRule testee = DockerRule.builder()//
            .imageName("busybox")//
            .mountFrom(testDir).to("/somedir", "ro")//
            .cmd("sh", "-c", "cat /somedir/"+testFilename)//
            .build();

    @Test(timeout = 10000)
    public void shouldReadMountFromJavaFile() throws Throwable {
        testee.waitFor("1234567890", 10);
    }

}
