package mvvm.example.core.viewmodel;

import javafx.beans.value.ObservableValue;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

import static java.util.Objects.requireNonNull;

public class Action {

    private final ReadOnlyBooleanWrapper canExecute = new ReadOnlyBooleanWrapper(this, "canExecute", true);
    private final Listener listener;

    // Strong reference prevents the binding from being garbage collected
    private final ObservableValue<? extends Boolean> binding;

    public Action(Listener listener) {
        this.listener = requireNonNull(listener);
        this.binding = null;
    }

    public Action(Listener listener, ObservableValue<? extends Boolean> binding) {
        this.listener = requireNonNull(listener);
        this.binding = requireNonNull(binding);
        this.canExecute.bind(binding);
    }

    public ReadOnlyBooleanProperty canExecuteProperty() {
        return canExecute.getReadOnlyProperty();
    }

    public boolean canExecute() {
        return canExecute.get();
    }

    public void execute() {
        if (canExecute()) {
            listener.actionExecuted();
        }
    }

    @FunctionalInterface
    public interface Listener {
        void actionExecuted();
    }
}
