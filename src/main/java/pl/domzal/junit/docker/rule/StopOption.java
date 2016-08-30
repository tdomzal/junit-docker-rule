package pl.domzal.junit.docker.rule;

import java.util.HashSet;
import java.util.Set;

/**
 * Container stop options. If not redefined active options are {@link #STOP}, {@link #REMOVE}.
 */
public enum StopOption {

    /**
     * Keep container instance at end. Opposite of {@link #REMOVE}.
     */
    KEEP,
    /**
    * Remove container instance at end. Opposite of {@link #KEEP}.
    */
    REMOVE,
    /**
     * Stop at end. Opposite of {@link #KILL}
     */
    STOP,
    /**
     * Kill at end. Opposite of {@link #STOP}.
     */
    KILL;

    static class StopOptionSet {

        private final Set<StopOption> currentOptions = new HashSet<>();

        StopOptionSet() {
            currentOptions.add(StopOption.STOP);
            currentOptions.add(StopOption.REMOVE);
        }

        public void setOptions(StopOption... newOptions) {
            for (StopOption option : newOptions) {
                currentOptions.add(option);
                if (StopOption.KEEP.equals(option)) {
                    currentOptions.remove(StopOption.REMOVE);
                } else if (StopOption.REMOVE.equals(option)) {
                    currentOptions.remove(StopOption.KEEP);
                } else if (StopOption.STOP.equals(option)) {
                    currentOptions.remove(StopOption.KILL);
                } else if (StopOption.KILL.equals(option)) {
                    currentOptions.remove(StopOption.STOP);
                }
            }
        }

        public boolean contains(StopOption stopOption) {
            return currentOptions.contains(stopOption);
        }
    }
}
