package mvvm.example.core.viewmodel;

import javafx.beans.property.SimpleBooleanProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Action")
class ActionTest {

    @Nested
    @DisplayName("when the action is always executable")
    class WithNoGuard {

        @Test
        @DisplayName("executes the listener when execute() is called")
        void executesTheListener() {
            var executed = new AtomicBoolean(false);
            var action = new Action(() -> executed.set(true));

            action.execute();

            assertTrue(executed.get());
        }

        @Test
        @DisplayName("canExecute() returns true")
        void canExecuteIsTrue() {
            var action = new Action(() -> {});

            assertTrue(action.canExecute());
        }
    }

    @Nested
    @DisplayName("when execution is conditional")
    class WithBooleanBinding {

        @Test
        @DisplayName("executes the listener when the binding is true")
        void executesWhenBindingIsTrue() {
            var executed = new AtomicBoolean(false);
            var canExecute = new SimpleBooleanProperty(true);
            var action = new Action(() -> executed.set(true), canExecute);

            action.execute();

            assertTrue(executed.get());
        }

        @Test
        @DisplayName("does not execute the listener when the binding is false")
        void doesNotExecuteWhenBindingIsFalse() {
            var executed = new AtomicBoolean(false);
            var canExecute = new SimpleBooleanProperty(false);
            var action = new Action(() -> executed.set(true), canExecute);

            action.execute();

            assertFalse(executed.get());
        }

        @Test
        @DisplayName("canExecute() is synchronised with the binding")
        void canExecuteReflectsBinding() {
            var canExecute = new SimpleBooleanProperty(false);
            var action = new Action(() -> {}, canExecute);

            assertFalse(action.canExecute());
        }

        @Test
        @DisplayName("canExecute() becomes true when the binding changes from false to true")
        void canExecuteUpdatesWhenBindingChanges() {
            var canExecute = new SimpleBooleanProperty(false);
            var action = new Action(() -> {}, canExecute);

            canExecute.set(true);

            assertTrue(action.canExecute());
        }
    }
}
