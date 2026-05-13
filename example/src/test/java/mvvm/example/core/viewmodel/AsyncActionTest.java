package mvvm.example.core.viewmodel;

import javafx.beans.property.SimpleBooleanProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AsyncAction")
class AsyncActionTest {

    @Nested
    @DisplayName("when the action is always executable")
    class WithNoGuard {

        @Test
        @DisplayName("canExecute() is true before any execution")
        void canExecuteIsTrueInitially() {
            var action = new AsyncAction(() -> CompletableFuture.completedFuture(() -> {}));

            assertTrue(action.canExecute());
        }

        @Test
        @DisplayName("executes the listener when executeAsync() is called")
        void executesTheListener() {
            var executed = new AtomicBoolean(false);
            var action = new AsyncAction(() -> {
                executed.set(true);
                return CompletableFuture.completedFuture(() -> {});
            });

            action.executeAsync(Runnable::run);

            assertTrue(executed.get());
        }
    }

    @Nested
    @DisplayName("while execution is in progress")
    class WhileExecuting {

        @Test
        @DisplayName("canExecute() returns false")
        void canExecuteIsFalseWhileExecuting() {
            var blocker = new CompletableFuture<Runnable>();
            var action = new AsyncAction(() -> blocker);

            action.executeAsync(Runnable::run);

            assertFalse(action.canExecute());
        }

        @Test
        @DisplayName("isExecuting() returns true")
        void isExecutingIsTrueWhileExecuting() {
            var blocker = new CompletableFuture<Runnable>();
            var action = new AsyncAction(() -> blocker);

            action.executeAsync(Runnable::run);

            assertTrue(action.isExecuting());
        }

        @Test
        @DisplayName("does not accept a second execution")
        void doesNotAcceptDoubleSubmission() {
            var callCount = new AtomicInteger(0);
            var blocker = new CompletableFuture<Runnable>();
            var action = new AsyncAction(() -> {
                callCount.incrementAndGet();
                return blocker;
            });

            action.executeAsync(Runnable::run);
            action.executeAsync(Runnable::run);

            assertEquals(1, callCount.get());
        }
    }

    @Nested
    @DisplayName("after execution completes")
    class AfterExecutionCompletes {

        @Test
        @DisplayName("canExecute() returns true")
        void canExecuteIsTrueAfterCompletion() {
            var blocker = new CompletableFuture<Runnable>();
            var action = new AsyncAction(() -> blocker);

            action.executeAsync(Runnable::run);
            blocker.complete(() -> {});

            assertTrue(action.canExecute());
        }

        @Test
        @DisplayName("isExecuting() returns false")
        void isExecutingIsFalseAfterCompletion() {
            var blocker = new CompletableFuture<Runnable>();
            var action = new AsyncAction(() -> blocker);

            action.executeAsync(Runnable::run);
            blocker.complete(() -> {});

            assertFalse(action.isExecuting());
        }
    }

    @Nested
    @DisplayName("when execution is conditional")
    class WithBooleanBinding {

        @Test
        @DisplayName("does not execute the listener when the binding is false")
        void doesNotExecuteWhenBindingIsFalse() {
            var executed = new AtomicBoolean(false);
            var canExecute = new SimpleBooleanProperty(false);
            var action = new AsyncAction(() -> {
                executed.set(true);
                return CompletableFuture.completedFuture(() -> {});
            }, canExecute);

            action.executeAsync(Runnable::run);

            assertFalse(executed.get());
        }

        @Test
        @DisplayName("canExecute() is synchronised with the binding")
        void canExecuteIsFalseWhenBindingIsFalse() {
            var canExecute = new SimpleBooleanProperty(false);
            var action = new AsyncAction(
                () -> CompletableFuture.completedFuture(() -> {}),
                canExecute
            );

            assertFalse(action.canExecute());
        }
    }
}
