package mvvm.example.shell;

import javafx.beans.property.ReadOnlyStringProperty;
import mvvm.example.core.viewmodel.Action;

public class WorkspaceTabViewModel {

    public static WorkspaceTabViewModel pinned(ReadOnlyStringProperty title, Object content) {
        return new WorkspaceTabViewModel(title, content, false);
    }

    public static WorkspaceTabViewModel closable(ReadOnlyStringProperty title, Object content) {
        return new WorkspaceTabViewModel(title, content, true);
    }

    private final ReadOnlyStringProperty title;
    private final Object content;
    private Action.Listener onClose = () -> {};
    private final Action closeAction;

    private WorkspaceTabViewModel(ReadOnlyStringProperty title, Object content, boolean closable) {
        this.title = title;
        this.content = content;
        this.closeAction = closable ? new Action(() -> onClose.actionExecuted()) : Action.disabled();
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
