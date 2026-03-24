package io.github.trae.hytale.framework.utility;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.World;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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

    /**
     * Schedules a {@link Runnable} to execute at a fixed rate on the server's
     * {@link HytaleServer#SCHEDULED_EXECUTOR} with an optional cancellation supplier.
     *
     * <p>The task will first execute after {@code initialDelay}, then repeatedly
     * every {@code period} measured from the <b>start</b> of the previous execution.
     * If a {@code cancelSupplier} is provided, it is checked before each invocation —
     * if it returns {@code true}, the scheduled task is cancelled and the runnable
     * will not execute.</p>
     *
     * @param runnable       the task to execute
     * @param initialDelay   the time to delay first execution
     * @param period         the period between successive executions
     * @param timeUnit       the time unit of the {@code initialDelay} and {@code period} parameters
     * @param cancelSupplier a supplier that returns {@code true} to cancel the scheduled task (may be {@code null})
     */
    public static void schedule(final Runnable runnable, final int initialDelay, final int period, final TimeUnit timeUnit, final Supplier<Boolean> cancelSupplier) {
        final AtomicReference<ScheduledFuture<?>> futureRef = new AtomicReference<>();

        final ScheduledFuture<?> future = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (cancelSupplier != null) {
                if (cancelSupplier.get()) {
                    final ScheduledFuture<?> self = futureRef.get();
                    if (self != null) {
                        self.cancel(false);
                    }
                    return;
                }
            }
            runnable.run();
        }, initialDelay, period, timeUnit);

        futureRef.set(future);
    }

    /**
     * Schedules a {@link Runnable} to execute at a fixed rate on the server's
     * {@link HytaleServer#SCHEDULED_EXECUTOR}.
     *
     * @param runnable     the task to execute
     * @param initialDelay the time to delay first execution
     * @param period       the period between successive executions
     * @param timeUnit     the time unit of the {@code initialDelay} and {@code period} parameters
     * @see #schedule(Runnable, int, int, TimeUnit, Supplier)
     */
    public static void schedule(final Runnable runnable, final int initialDelay, final int period, final TimeUnit timeUnit) {
        schedule(runnable, initialDelay, period, timeUnit, null);
    }

    /**
     * Schedules a {@link Runnable} to execute at a fixed rate, with each invocation
     * offloaded asynchronously via {@link #executeAsynchronous(Runnable)}, with an
     * optional cancellation supplier.
     *
     * <p>The scheduled executor acts purely as a timer — the actual work runs on
     * the common {@link java.util.concurrent.ForkJoinPool}, preventing long-running
     * tasks from blocking the scheduler thread. If a {@code cancelSupplier} is provided,
     * it is checked before each invocation — if it returns {@code true}, the scheduled
     * task is cancelled and the runnable will not execute.</p>
     *
     * @param runnable       the task to execute asynchronously on each tick
     * @param initialDelay   the time to delay first execution
     * @param period         the period between successive executions
     * @param timeUnit       the time unit of the {@code initialDelay} and {@code period} parameters
     * @param cancelSupplier a supplier that returns {@code true} to cancel the scheduled task (may be {@code null})
     */
    public static void scheduleAsynchronous(final Runnable runnable, final int initialDelay, final int period, final TimeUnit timeUnit, final Supplier<Boolean> cancelSupplier) {
        final AtomicReference<ScheduledFuture<?>> futureRef = new AtomicReference<>();

        final ScheduledFuture<?> future = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (cancelSupplier != null) {
                if (cancelSupplier.get()) {
                    final ScheduledFuture<?> self = futureRef.get();
                    if (self != null) {
                        self.cancel(false);
                    }
                    return;
                }
            }
            executeAsynchronous(runnable);
        }, initialDelay, period, timeUnit);

        futureRef.set(future);
    }

    /**
     * Schedules a {@link Runnable} to execute at a fixed rate, with each invocation
     * offloaded asynchronously via {@link #executeAsynchronous(Runnable)}.
     *
     * @param runnable     the task to execute asynchronously on each tick
     * @param initialDelay the time to delay first execution
     * @param period       the period between successive executions
     * @param timeUnit     the time unit of the {@code initialDelay} and {@code period} parameters
     * @see #scheduleAsynchronous(Runnable, int, int, TimeUnit, Supplier)
     */
    public static void scheduleAsynchronous(final Runnable runnable, final int initialDelay, final int period, final TimeUnit timeUnit) {
        scheduleAsynchronous(runnable, initialDelay, period, timeUnit, null);
    }
}