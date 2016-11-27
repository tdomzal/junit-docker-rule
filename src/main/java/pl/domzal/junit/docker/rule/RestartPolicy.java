package pl.domzal.junit.docker.rule;

import com.spotify.docker.client.messages.HostConfig;

/**
 * Container restart policy. Possible should be created with
 * {@link #always()}, {@link #unlessStopped()} or {@link #onFailure(int)}.
 */
public final class RestartPolicy {

    private final HostConfig.RestartPolicy restartPolicy;

    public RestartPolicy(HostConfig.RestartPolicy restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public HostConfig.RestartPolicy getRestartPolicy() {
        return restartPolicy;
    }

    /**
     * 'onFailure' restart policy with specified maximum number of retries.
     *
     * @param maxRetryCount Number of retries.
     */
    public static RestartPolicy onFailure(int maxRetryCount) {
        return new RestartPolicy(HostConfig.RestartPolicy.onFailure(maxRetryCount));
    }

    /**
     * 'unlessStopped' restart policy.
     */
    public static RestartPolicy unlessStopped() {
        return new RestartPolicy(HostConfig.RestartPolicy.unlessStopped());
    }

    /**
     * 'always' restart policy.
     */
    public static RestartPolicy always() {
        return new RestartPolicy(HostConfig.RestartPolicy.always());
    }
}
