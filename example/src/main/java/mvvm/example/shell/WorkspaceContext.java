package mvvm.example.shell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class WorkspaceContext {

    private final ObjectProperty<Object> currentWorkspaceProperty = new SimpleObjectProperty<>(this, "currentWorkspace");

    public void show(Object viewModel) {
        currentWorkspaceProperty.set(viewModel);
    }

    public ReadOnlyObjectProperty<Object> currentWorkspaceProperty() {
        return currentWorkspaceProperty;
    }
}
