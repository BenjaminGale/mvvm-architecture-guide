package mvvm.example.shell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Supplier;

public class WorkspaceContext {

    private final ObjectProperty<Object> currentWorkspaceProperty = new SimpleObjectProperty<>(this, "currentWorkspace");
    private final ObservableList<ReadOnlyStringProperty> statusMessages = FXCollections.observableArrayList();

    public void show(Supplier<Object> factory) {
        statusMessages.clear();
        currentWorkspaceProperty.set(factory.get());
    }

    public ReadOnlyObjectProperty<Object> currentWorkspaceProperty() {
        return currentWorkspaceProperty;
    }

    public ObservableList<ReadOnlyStringProperty> statusMessages() {
        return statusMessages;
    }
}
