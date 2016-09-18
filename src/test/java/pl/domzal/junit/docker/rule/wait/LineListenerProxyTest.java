package pl.domzal.junit.docker.rule.wait;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.Test;

public class LineListenerProxyTest {

    @Test
    public void shouldProxyToListeners() throws Exception {
        // given
        LineListenerProxy testee = new LineListenerProxy();
        LineListener l1 = mock(LineListener.class);
        LineListener l2 = mock(LineListener.class);
        testee.addAll(Arrays.asList(l1, l2));
        // when
        testee.nextLine("one");
        testee.nextLine("two");
        // then
        verify(l1).nextLine(eq("one"));
        verify(l1).nextLine(eq("two"));
        verify(l2).nextLine(eq("one"));
        verify(l2).nextLine(eq("two"));
    }

    @Test
    public void shouldProxyToLateListeners() {
        // given
        LineListenerProxy testee = new LineListenerProxy();
        LineListener l1 = mock(LineListener.class);
        testee.add(l1);
        // when
        testee.nextLine("one");
        LineListener l2 = mock(LineListener.class);
        testee.add(l2);
        testee.nextLine("two");
        // then
        verify(l1).nextLine(eq("one"));
        verify(l1).nextLine(eq("two"));
        verify(l2).nextLine(eq("one"));
        verify(l2).nextLine(eq("two"));
    }

    @Test
    public void shouldNotProxyToLateListenersOutsideHistoryLimit() {
        // given
        final int HISTORY_LIMIT = 4;
        LineListenerProxy testee = new LineListenerProxy(HISTORY_LIMIT);
        LineListener l1 = mock(LineListener.class);
        testee.add(l1);
        // when
        testee.nextLine("one");
        testee.nextLine("two");
        testee.nextLine("three");
        testee.nextLine("four");
        testee.nextLine("five");
        LineListener l2 = mock(LineListener.class);
        testee.add(l2);
        // then
        verify(l1).nextLine(eq("one"));
        verify(l1).nextLine(eq("two"));
        verify(l1).nextLine(eq("three"));
        verify(l1).nextLine(eq("four"));
        verify(l1).nextLine(eq("five"));

        verify(l2, never()).nextLine(eq("one"));
        verify(l2).nextLine(eq("two"));
        verify(l2).nextLine(eq("three"));
        verify(l2).nextLine(eq("four"));
        verify(l2).nextLine(eq("five"));
    }
}