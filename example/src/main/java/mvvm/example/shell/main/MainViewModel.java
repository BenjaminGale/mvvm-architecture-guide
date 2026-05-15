package mvvm.example.shell.main;

import javafx.beans.property.ReadOnlyObjectProperty;
import mvvm.example.shell.WorkspaceContext;
import mvvm.example.shell.sidebar.SidebarViewModel;

public class MainViewModel {

    private final SidebarViewModel sidebar;
    private final WorkspaceContext workspaceContext;

    public MainViewModel(SidebarViewModel sidebar, WorkspaceContext workspaceContext) {
        this.sidebar = sidebar;
        this.workspaceContext = workspaceContext;
    }

    public SidebarViewModel getSidebar() {
        return sidebar;
    }

    public ReadOnlyObjectProperty<Object> currentWorkspaceProperty() {
        return workspaceContext.currentWorkspaceProperty();
    }
}
