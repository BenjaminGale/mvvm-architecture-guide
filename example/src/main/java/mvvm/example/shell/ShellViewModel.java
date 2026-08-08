package mvvm.example.shell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ShellViewModel {

    private final ObservableList<WorkspaceViewModel> workspaces;
    private final ObjectProperty<WorkspaceViewModel> currentWorkspace = new SimpleObjectProperty<>(this, "currentWorkspace");

    public ShellViewModel(List<WorkspaceViewModel> workspaces) {
        this.workspaces = FXCollections.observableArrayList(workspaces);
        this.workspaces.forEach(workspace -> workspace.onOpen(() -> select(workspace)));

        if (!this.workspaces.isEmpty()) {
            select(this.workspaces.getFirst());
        }
    }

    public ObservableList<WorkspaceViewModel> workspaces() {
        return workspaces;
    }

    public ReadOnlyObjectProperty<WorkspaceViewModel> currentWorkspaceProperty() {
        return currentWorkspace;
    }

    public void select(WorkspaceViewModel workspace) {
        currentWorkspace.set(workspace);
    }
}
