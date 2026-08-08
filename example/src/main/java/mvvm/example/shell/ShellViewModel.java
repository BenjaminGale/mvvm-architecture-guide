package mvvm.example.shell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ShellViewModel {

    private final ObservableList<WorkspaceViewModel> workspaces = FXCollections.observableArrayList();
    private final ObjectProperty<WorkspaceViewModel> currentWorkspace = new SimpleObjectProperty<>(this, "currentWorkspace");

    public ObservableList<WorkspaceViewModel> workspaces() {
        return workspaces;
    }

    public ReadOnlyObjectProperty<WorkspaceViewModel> currentWorkspaceProperty() {
        return currentWorkspace;
    }

    public void registerWorkspace(String title, WorkspaceTabViewModel explorerTab) {
        var workspace = new WorkspaceViewModel(title, explorerTab);
        workspace.onOpen(() -> select(workspace));

        workspaces.add(workspace);
        if (workspaces.size() == 1) {
            select(workspace);
        }
    }

    public void select(WorkspaceViewModel workspace) {
        currentWorkspace.set(workspace);
    }
}
