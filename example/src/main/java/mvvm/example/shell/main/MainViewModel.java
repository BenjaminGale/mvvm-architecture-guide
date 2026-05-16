package mvvm.example.shell.main;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import mvvm.example.shell.WorkspaceContext;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;
import mvvm.example.shell.main.sidebar.SidebarViewModel;

public class MainViewModel {

    private final SidebarViewModel sidebar;
    private final WorkspaceContext workspaces;

    public MainViewModel(
        SidebarViewModel sidebar,
        WorkspaceContext workspaces
    ) {
        this.sidebar = sidebar;
        this.workspaces = workspaces;
    }

    public SidebarViewModel getSidebar() {
        return sidebar;
    }

    public ReadOnlyObjectProperty<Object> currentWorkspaceProperty() {
        return workspaces.currentWorkspaceProperty();
    }

    public ObservableList<StatusItemViewModel> statusItems() {
        return workspaces.statusItems();
    }
}
