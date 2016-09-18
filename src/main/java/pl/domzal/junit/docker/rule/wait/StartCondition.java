package pl.domzal.junit.docker.rule.wait;

import pl.domzal.junit.docker.rule.DockerRule;

/**
 * Builder of {@link StartConditionCheck} instances. Usually condition
 * must have access to started container so builder is easy way to supply
 * one at rule configuration phase.
 */
public interface StartCondition {
    /**
     * Create {@link StartConditionCheck} instances for current {@link DockerRule}.
     */
    StartConditionCheck build(DockerRule currentRule);
}
