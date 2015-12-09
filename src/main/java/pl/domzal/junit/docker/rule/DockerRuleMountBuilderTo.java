package pl.domzal.junit.docker.rule;

public interface DockerRuleMountBuilderTo {
    /**
     * Mount to point.
     *
     * @param toPath Container target path
     * @param mode Mount mode ('ro' or 'rw').
     */
    DockerRuleBuiler to(String toPath, String mode);
    /**
     * Mount to point (mounting in default 'rw' mode).
     *
     * @param toPath Container target path
     */
    DockerRuleBuiler to(String path);
}
