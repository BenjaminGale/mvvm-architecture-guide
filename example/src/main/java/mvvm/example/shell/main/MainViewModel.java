package mvvm.example.shell.main;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import mvvm.example.shell.ShellContext;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;
import mvvm.example.shell.main.sidebar.SidebarViewModel;

public class MainViewModel {

    private final SidebarViewModel sidebar;
    private final ShellContext shell;

    public MainViewModel(
        SidebarViewModel sidebar,
        ShellContext shell
    ) {
        this.sidebar = sidebar;
        this.shell = shell;
    }

    public SidebarViewModel getSidebar() {
        return sidebar;
    }

    public ReadOnlyObjectProperty<Object> currentWorkspaceProperty() {
        return shell.currentWorkspaceProperty();
    }

    public ObservableList<StatusItemViewModel> statusItems() {
        return shell.statusItems();
    }
}
