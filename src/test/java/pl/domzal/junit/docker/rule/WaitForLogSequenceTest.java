package pl.domzal.junit.docker.rule;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(test.category.Stable.class)
public class WaitForLogSequenceTest {

    public static final int TIMEOUT_NOT_USED = 0;

    @Test
    public void shouldDetectSequence() {
        WaitForLogSequence testee = new WaitForLogSequence(Arrays.asList("one", "three"), TIMEOUT_NOT_USED);

        assertFalse(testee.sequenceFound());
        testee.nextLine("one");
        testee.nextLine("two");
        assertFalse(testee.sequenceFound());
        testee.nextLine("three");
        assertTrue(testee.sequenceFound());
        testee.nextLine("four");
        assertTrue(testee.sequenceFound());
    }

    @Test
    public void shouldDetectEmptySequence() {
        WaitForLogSequence testee = new WaitForLogSequence(Arrays.<String>asList(), TIMEOUT_NOT_USED);

        assertTrue(testee.sequenceFound());
    }
}