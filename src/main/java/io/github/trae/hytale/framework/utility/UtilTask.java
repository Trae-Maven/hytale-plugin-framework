package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.World;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Utility class for executing tasks across different threading contexts.
 *
 * <p>Provides methods for immediate, synchronous (scheduled executor), asynchronous
 * ({@link CompletableFuture}), and world-thread execution. World-thread methods
 * include a fast path when the caller is already on the target world's thread.</p>
 */
@UtilityClass
public class UtilTask {

    /**
     * Executes a {@link Runnable} immediately on the calling thread.
     *
     * @param runnable the task to execute
     * @throws IllegalArgumentException if {@code runnable} is {@code null}
     */
    public static void execute(final Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null.");
        }

        runnable.run();
    }

    /**
     * Submits a {@link Runnable} to the server's {@link HytaleServer#SCHEDULED_EXECUTOR}.
     *
     * <p>The task will be executed on the scheduled executor's thread pool,
     * typically used for server-tick-aligned or delayed work.</p>
     *
     * @param runnable the task to submit
     * @throws IllegalArgumentException if {@code runnable} is {@code null}
     */
    public static void executeSynchronous(final Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null.");
        }

        HytaleServer.SCHEDULED_EXECUTOR.execute(runnable);
    }

    /**
     * Executes a {@link Runnable} asynchronously using the common {@link java.util.concurrent.ForkJoinPool}.
     *
     * <p>Delegates to {@link CompletableFuture#runAsync(Runnable)}. The returned future
     * is intentionally discarded; use this for fire-and-forget async work.</p>
     *
     * @param runnable the task to execute asynchronously
     * @throws IllegalArgumentException if {@code runnable} is {@code null}
     */
    public static void executeAsynchronous(final Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null.");
        }

        CompletableFuture.runAsync(runnable);
    }

    /**
     * Executes a {@link Runnable} on the specified world's thread.
     *
     * <p>If the caller is already on the world's thread (determined by
     * {@link World#isInThread()}), the runnable is executed immediately.
     * Otherwise, it is scheduled via {@link World#execute(Runnable)}.</p>
     *
     * @param world    the target world whose thread the task should run on
     * @param runnable the task to execute
     * @throws IllegalArgumentException if {@code world} or {@code runnable} is {@code null}
     */
    public static void executeByWorld(final World world, final Runnable runnable) {
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null.");
        }

        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null.");
        }

        // Fast path: already on the world thread, execute directly
        if (world.isInThread()) {
            execute(runnable);
            return;
        }

        // Schedule execution on the world's thread
        world.execute(runnable);
    }

    /**
     * Supplies a value by executing a {@link Supplier} on the specified world's thread,
     * returning the result as a {@link CompletableFuture}.
     *
     * <p>If the caller is already on the world's thread, the supplier is invoked
     * immediately and wrapped in a pre-completed future. Otherwise, an incomplete
     * future is returned and completed once the supplier executes on the world thread.</p>
     *
     * <p>Exceptions thrown by the supplier are captured and propagated via
     * {@link CompletableFuture#completeExceptionally(Throwable)}.</p>
     *
     * @param world    the target world whose thread the supplier should run on
     * @param supplier the value-producing function
     * @param <T>      the type of the supplied value
     * @return a {@link CompletableFuture} that resolves to the supplier's result
     * @throws IllegalArgumentException if {@code world} or {@code supplier} is {@code null}
     */
    public static <T> CompletableFuture<T> supplyByWorld(final World world, final Supplier<T> supplier) {
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null.");
        }

        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null.");
        }

        // Fast path: already on the world thread, resolve immediately
        if (world.isInThread()) {
            return CompletableFuture.completedFuture(supplier.get());
        }

        // Create an incomplete future to be completed on the world thread
        final CompletableFuture<T> completableFuture = new CompletableFuture<>();

        world.execute(() -> {
            try {
                completableFuture.complete(supplier.get());
            } catch (final Exception e) {
                completableFuture.completeExceptionally(e);
            }
        });

        return completableFuture;
    }
}