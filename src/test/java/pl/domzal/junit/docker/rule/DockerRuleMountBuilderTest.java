package pl.domzal.junit.docker.rule;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

public class DockerRuleMountBuilderTest {

    @Test
    public void assertMountShouldAcceptOsxHomePath() throws Exception {
        DockerRuleMountBuilder.assertValidMountFrom("/Users/someuser", "/Users/someuser/temp", true, false);
    }

    @Test(expected = InvalidVolumeFrom.class)
    public void assertMountShouldFailOnOsxOutsideHomePath() throws Exception {
        DockerRuleMountBuilder.assertValidMountFrom("/Users/someuser", "/tmp", true, false);
    }

    @Test
    public void assertMountShouldAcceptWindowsHomePath() throws Exception {
        DockerRuleMountBuilder.assertValidMountFrom("C:\\Users\\someuser", "/c/Users/someuser/temp", false, true);
    }

    @Test(expected = InvalidVolumeFrom.class)
    public void assertMountShouldFailOnWindowsOutsideHomePath() throws Exception {
        DockerRuleMountBuilder.assertValidMountFrom("C:\\Users\\someuser", "/c/tmp", false, true);
    }

    @Test
    public void assertMountShouldAcceptWinHomeFoundFromEnv() {
        // sanity check if fetching user home on windows works as expected
        // makes sense only on windows
        if (SystemUtils.IS_OS_WINDOWS) {
            Map<String, String> env = System.getenv();
            String windowsHomeUnixStyle = DockerRuleMountBuilder.toUnixStylePath(env.get("HOMEDRIVE") + env.get("HOMEPATH"));
            DockerRuleMountBuilder.assertValidMountFrom(SystemUtils.getUserHome().getAbsolutePath(), windowsHomeUnixStyle, false, true);
        }
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