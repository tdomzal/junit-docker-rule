package pl.domzal.junit.docker.rule;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DockerRuleTodoTest {

    // mounts
    @Test
    public void shouldMountLocalVolume() {
        fail("Not yet implemented");
    }

    @Test
    public void shouldFailMountLocalVolumeOnRemoteDockerHost() {
        fail("Not yet implemented");
    }

    // image pull
    @Test
    public void shouldFailPullMissingImageIfNoPullOptionEnforced() {
        fail("Not yet implemented");
    }

    @Test
    public void shouldWaitForUrlAvailable() {
        fail("Not yet implemented");
    }

}
