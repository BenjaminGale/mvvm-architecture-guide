package mvvm.example.shell.main.sidebar;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import mvvm.example.core.viewmodel.Action;

public class SidebarItemViewModel {

    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title");
    private final Action action;

    public SidebarItemViewModel(String title, Action.Listener listener) {
        this.title.set(title);
        this.action = new Action(listener);
    }

    public Action action() {
        return action;
    }

    public ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }
}
