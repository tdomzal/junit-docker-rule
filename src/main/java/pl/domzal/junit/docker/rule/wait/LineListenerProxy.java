package pl.domzal.junit.docker.rule.wait;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * {@link LineListener} that can proxy line to set of underlying
 * listeners with additional skill of replying lines history to listeners
 * registered later.
 * Replayed history size is limited to {@value #DEFAULT_HISTORY_LIMIT}.
 */
public class LineListenerProxy implements LineListener {

    private static final int DEFAULT_HISTORY_LIMIT = 1000;

    private final int historyLimit;

    /**
     * Lines already proxied to listeners. Allows passing lines from the past to
     * late registered listeners. Size of history is limited to {@link #DEFAULT_HISTORY_LIMIT}. */
    private final List<String> history;

    /** Points to first empty slot in {@link #history}. */
    private int historyFirstFreeSlot = 0;

    private final List<LineListener> listeners = Lists.newArrayList();

    public LineListenerProxy() {
        this(DEFAULT_HISTORY_LIMIT);
    }

    LineListenerProxy(int historyLimit) {
        this.historyLimit = historyLimit;
        this.history = Lists.newLinkedList();
    }

    @Override
    public synchronized void nextLine(String line) {
        // bufer limit reached ?
        if (historyFirstFreeSlot == historyLimit) {
            // remove oldest message
            history.remove(0);
            // current empty slot will be one before
            historyFirstFreeSlot--;
        }
        // append next line to history and update pointer to next free slot
        history.add(line);
        historyFirstFreeSlot++;
        // pass line to current listeners
        for (LineListener listener : listeners) {
            listener.nextLine(line);
        }
    }

    public synchronized void addAll(List<LineListener> lineListeners) {
        // reply history to new listeners
        for (int i = 0; i < historyFirstFreeSlot; i++) {
            for (LineListener newListener : lineListeners) {
                newListener.nextLine(history.get(i));
            }
        }
        // register listeners
        listeners.addAll(lineListeners);
    }

    public synchronized void add(LineListener listener) {
        // reply history
        for (int i = 0; i < historyFirstFreeSlot; i++) {
            listener.nextLine(history.get(i));
        }
        // register
        listeners.add(listener);
    }

}
