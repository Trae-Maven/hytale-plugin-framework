package io.github.trae.hytale.framework.helper.interfaces;

/**
 * Interface for components that require a deferred processing step.
 *
 * <p>Implementations accumulate registrations or state changes, then apply
 * them in bulk when {@link #process()} is invoked. This is used by helpers
 * such as {@link io.github.trae.hytale.framework.helper.CommandHelper} to
 * batch-register commands after all have been queued.</p>
 */
public interface Processable {

    /**
     * Processes all pending registrations or state changes.
     *
     * <p>Implementations should be idempotent — calling {@code process()}
     * multiple times should not produce duplicate registrations.</p>
     */
    void process();
}