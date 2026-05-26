package mvvm.example.shell.main.sidebar;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import mvvm.example.core.viewmodel.Action;

public class SidebarItemViewModel {

    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title");
    private final Action openWorkspaceAction;

    public SidebarItemViewModel(String title, Action.Listener openWorkspace) {
        this.title.set(title);
        this.openWorkspaceAction = new Action(openWorkspace);
    }

    public Action openWorkspaceAction() {
        return openWorkspaceAction;
    }

    public ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }
}
