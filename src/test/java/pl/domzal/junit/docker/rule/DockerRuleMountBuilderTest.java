package pl.domzal.junit.docker.rule;

import static org.junit.Assert.*;

import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import pl.domzal.junit.docker.rule.ex.InvalidVolumeFrom;

@Category(test.category.Stable.class)
public class DockerRuleMountBuilderTest {

    @Test
    public void shouldAcceptValidDockerVolume() throws Exception {
        DockerRuleMountBuilder.assertValidMountFrom("/Users/someuser", "abcdef", true, false);
    }

    @Test
    public void shouldAcceptOsxHomePath() throws Exception {
        DockerRuleMountBuilder.assertValidMountFrom("/Users/someuser", "/Users/someuser/temp", true, false);
    }

    @Test(expected = InvalidVolumeFrom.class)
    public void shouldFailOnOsxOutsideHomePath() throws Exception {
        DockerRuleMountBuilder.assertValidMountFrom("/Users/someuser", "/tmp", true, false);
    }

    @Test
    public void shouldAcceptWindowsHomePath() throws Exception {
        DockerRuleMountBuilder.assertValidMountFrom("C:\\Users\\someuser", "/c/Users/someuser/temp", false, true);
    }

    @Test(expected = InvalidVolumeFrom.class)
    public void shouldFailOnWindowsOutsideHomePath() throws Exception {
        DockerRuleMountBuilder.assertValidMountFrom("C:\\Users\\someuser", "/c/tmp", false, true);
    }

    @Test
    public void shouldAcceptWinHomeFoundFromEnv() {
        // sanity check if fetching user home on windows works as expected
        // makes sense only on windows
        if (SystemUtils.IS_OS_WINDOWS) {
            Map<String, String> env = System.getenv();
            String windowsHomeUnixStyle = DockerRuleMountBuilder.toUnixStylePath(env.get("HOMEDRIVE") + env.get("HOMEPATH"));
            DockerRuleMountBuilder.assertValidMountFrom(SystemUtils.getUserHome().getAbsolutePath(), windowsHomeUnixStyle, false, true);
        }
    }

    @Test
    public void assertValidDockerVolumes() {
        assertTrue(DockerRuleMountBuilder.isValidDockerVolumeName("a"));
        assertTrue(DockerRuleMountBuilder.isValidDockerVolumeName("a.-"));
    }

    @Test
    public void assertInvalidDockerVolumes() {
        assertFalse(DockerRuleMountBuilder.isValidDockerVolumeName("a/"));
        assertFalse(DockerRuleMountBuilder.isValidDockerVolumeName("-"));
    }

    @Test
    public void toUnixStylePathShouldConvertWindowsPathToUnixStyle() {
        assertEquals("/c/Users/someuser", DockerRuleMountBuilder.toUnixStylePath("C:\\Users\\someuser"));
    }

    @Test
    public void toUnixStylePathShouldLeaveUnixStylePath() {
        assertEquals("/home/someuser", DockerRuleMountBuilder.toUnixStylePath("/home/someuser"));
    }

    @Test(expected = IllegalStateException.class)
    public void toUnixStylePathShouldFailOnEmptyPath() {
        DockerRuleMountBuilder.toUnixStylePath("");
    }

    @Test(expected = IllegalStateException.class)
    public void toUnixStylePathShouldFailOnNullPath() {
        DockerRuleMountBuilder.toUnixStylePath("");
    }

}