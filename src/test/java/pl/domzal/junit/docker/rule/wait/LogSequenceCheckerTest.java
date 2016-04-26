package pl.domzal.junit.docker.rule.wait;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(test.category.Stable.class)
public class LogSequenceCheckerTest {

    @Test
    public void shouldDetectSequence() {
        LogSequenceChecker testee = new LogSequenceChecker(Arrays.asList("one", "three"));

        assertFalse(testee.check());
        testee.nextLine("one");
        testee.nextLine("two");
        assertFalse(testee.check());
        testee.nextLine("three");
        assertTrue(testee.check());
        testee.nextLine("four");
        assertTrue(testee.check());
    }

    @Test
    public void shouldDetectEmptySequence() {
        LogSequenceChecker testee = new LogSequenceChecker(Arrays.<String>asList());

        assertTrue(testee.check());
    }
}