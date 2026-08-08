package mvvm.example.shell;

import javafx.beans.property.ReadOnlyStringProperty;
import mvvm.example.core.viewmodel.Action;

import java.util.function.BooleanSupplier;

public class WorkspaceTabViewModel {

    public static WorkspaceTabViewModel unclosable(ReadOnlyStringProperty title, Object content) {
        return new WorkspaceTabViewModel(title, content, Action.disabled());
    }

    public static WorkspaceTabViewModel closable(ReadOnlyStringProperty title, Object content) {
        return closable(title, content, () -> true);
    }

    public static WorkspaceTabViewModel closable(ReadOnlyStringProperty title, Object content, BooleanSupplier canClose) {
        return new WorkspaceTabViewModel(title, content, canClose);
    }

    private final ReadOnlyStringProperty title;
    private final Object content;
    private Action.Listener onClose = () -> {};
    private final Action closeAction;

    private WorkspaceTabViewModel(ReadOnlyStringProperty title, Object content, Action closeAction) {
        this.title = title;
        this.content = content;
        this.closeAction = closeAction;
    }

    private WorkspaceTabViewModel(ReadOnlyStringProperty title, Object content, BooleanSupplier canClose) {
        this.title = title;
        this.content = content;
        this.closeAction = new Action(() -> {
            if (canClose.getAsBoolean()) onClose.actionExecuted();
        });
    }

    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    public Object content() {
        return content;
    }

    public Action closeAction() {
        return closeAction;
    }

    public void onClose(Action.Listener listener) {
        this.onClose = listener;
    }
}
