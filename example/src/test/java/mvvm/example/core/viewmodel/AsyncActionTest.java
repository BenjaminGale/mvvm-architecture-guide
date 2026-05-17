package mvvm.example.core.viewmodel;

import javafx.beans.property.SimpleBooleanProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ViewModel.AsyncAction")
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
        @DisplayName("throws on a second execution attempt")
        void throwsOnDoubleSubmission() {
            var blocker = new CompletableFuture<Runnable>();
            var action = new AsyncAction(() -> blocker);

            action.executeAsync(Runnable::run);

            assertThrows(IllegalStateException.class, () -> action.executeAsync(Runnable::run));
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
        @DisplayName("throws when executeAsync() is called and the binding is false")
        void throwsWhenBindingIsFalse() {
            var canExecute = new SimpleBooleanProperty(false);
            var action = new AsyncAction(
                () -> CompletableFuture.completedFuture(() -> {}),
                canExecute
            );

            assertThrows(IllegalStateException.class, () -> action.executeAsync(Runnable::run));
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
