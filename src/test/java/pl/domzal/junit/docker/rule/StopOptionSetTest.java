package pl.domzal.junit.docker.rule;

import static org.junit.Assert.*;

import org.junit.Test;

public class StopOptionSetTest {

    private StopOption.StopOptionSet testee = new StopOption.StopOptionSet();

    @Test
    public void shouldSetDefaultOptions() throws Exception {
        assertTrue(testee.contains(StopOption.STOP));
        assertTrue(testee.contains(StopOption.REMOVE));
    }

    @Test
    public void shouldRemoveOppositeToKeep() throws Exception {
        testee.setOptions(StopOption.KEEP);
        assertFalse(testee.contains(StopOption.REMOVE));
    }

    @Test
    public void shouldRemoveOppositeToKill() throws Exception {
        testee.setOptions(StopOption.KILL);
        assertFalse(testee.contains(StopOption.STOP));
    }
}