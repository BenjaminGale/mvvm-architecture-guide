package mvvm.example.shell.main.sidebar;

import javafx.beans.property.*;
import mvvm.example.core.viewmodel.Action;

public class SidebarItemViewModel {

    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final IntegerProperty count = new SimpleIntegerProperty(this, "count");
    private final Action openWorkspaceAction;

    public SidebarItemViewModel(String title, Action.Listener openWorkspace) {
        this.title.set(title);
        this.openWorkspaceAction = new Action(openWorkspace);
    }

    public Action openWorkspaceAction() {
        return openWorkspaceAction;
    }

    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    public ReadOnlyIntegerProperty countProperty() {
        return count;
    }
}
