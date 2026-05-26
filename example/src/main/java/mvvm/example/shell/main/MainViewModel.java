package mvvm.example.shell.main;

import javafx.beans.property.ReadOnlyObjectProperty;
import mvvm.example.shell.main.statusbar.StatusBarViewModel;
import mvvm.example.shell.main.sidebar.SidebarViewModel;

public class MainViewModel {

    private final SidebarViewModel sidebar;
    private final StatusBarViewModel statusBar;
    private final ReadOnlyObjectProperty<Object> currentWorkspace;

    public MainViewModel(
        SidebarViewModel sidebar,
        StatusBarViewModel statusBar,
        ReadOnlyObjectProperty<Object> currentWorkspace
    ) {
        this.sidebar = sidebar;
        this.statusBar = statusBar;
        this.currentWorkspace = currentWorkspace;
    }

    public SidebarViewModel sidebar() {
        return sidebar;
    }

    public StatusBarViewModel statusBar() {
        return statusBar;
    }

    public ReadOnlyObjectProperty<Object> currentWorkspaceProperty() {
        return currentWorkspace;
    }
}
