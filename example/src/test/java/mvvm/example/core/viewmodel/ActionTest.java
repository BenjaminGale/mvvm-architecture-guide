package mvvm.example.core.viewmodel;

import javafx.beans.property.SimpleBooleanProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ViewModel.Action")
class ActionTest {

    @Nested
    @DisplayName("when the action is always executable")
    class WithNoGuard {

        @Test
        @DisplayName("executes the listener when execute() is called")
        void executesTheListener() {
            Action.Listener listener = mock();
            var action = new Action(listener);

            action.execute();

            verify(listener).actionExecuted();
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
            Action.Listener listener = mock();
            var canExecute = new SimpleBooleanProperty(true);
            var action = new Action(listener, canExecute);

            action.execute();

            verify(listener).actionExecuted();
        }

        @Test
        @DisplayName("throws when execute() is called and the binding is false")
        void throwsWhenBindingIsFalse() {
            var canExecute = new SimpleBooleanProperty(false);
            var action = new Action(() -> {}, canExecute);

            assertThrows(IllegalStateException.class, action::execute);
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
