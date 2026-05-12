package mvvm.example.core.viewmodel;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableBooleanValue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.Objects.requireNonNull;

public class AsyncAction {

    private final ReadOnlyBooleanWrapper canExecuteProperty =
        new ReadOnlyBooleanWrapper(this, "canExecute", true);

    private final ReadOnlyBooleanWrapper isExecutingProperty =
        new ReadOnlyBooleanWrapper(this, "isExecuting", false);

    private final BooleanBinding canActionExecuteBinding =
        Bindings.createBooleanBinding(() -> !isExecuting(), isExecutingProperty);

    private final Listener listener;

    public AsyncAction(Listener listener) {
        this.listener = requireNonNull(listener);
        canExecuteProperty.bind(canActionExecuteBinding);
    }

    public AsyncAction(Listener listener, ObservableBooleanValue canExecuteBinding) {
        this.listener = requireNonNull(listener);
        requireNonNull(canExecuteBinding);
        canExecuteProperty.bind(canActionExecuteBinding.and(canExecuteBinding));
    }

    public CompletableFuture<Void> executeAsync(Executor viewExecutor) {
        requireNonNull(viewExecutor);

        if (!canExecute()) {
            return CompletableFuture.completedFuture(null);
        }

        isExecutingProperty.set(true);

        return listener
            .actionExecutedAsync()
            .whenCompleteAsync((result, exception) -> {
                if (result != null) result.run();
                isExecutingProperty.set(false);
            }, viewExecutor)
            .thenApply(ignored -> null);
    }

    public ReadOnlyBooleanProperty canExecuteProperty() {
        return canExecuteProperty.getReadOnlyProperty();
    }

    public boolean canExecute() {
        return canExecuteProperty.get();
    }

    public ReadOnlyBooleanProperty isExecutingProperty() {
        return isExecutingProperty.getReadOnlyProperty();
    }

    public boolean isExecuting() {
        return isExecutingProperty.get();
    }

    @FunctionalInterface
    public interface Listener {
        CompletableFuture<Runnable> actionExecutedAsync();
    }
}
