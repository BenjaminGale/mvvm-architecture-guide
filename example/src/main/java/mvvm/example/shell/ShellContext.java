package mvvm.example.shell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;

import java.util.function.Supplier;

public class ShellContext {

    private final ObservableList<SidebarItemViewModel> navigationItems = FXCollections.observableArrayList();
    private final ObjectProperty<Object> currentWorkspaceProperty = new SimpleObjectProperty<>(this, "currentWorkspace");
    private final ObservableList<StatusItemViewModel> statusItems = FXCollections.observableArrayList();

    public void show(Supplier<Object> factory) {
        statusItems.clear();
        currentWorkspaceProperty.set(factory.get());
    }

    public ObservableList<SidebarItemViewModel> navigationItems() {
        return navigationItems;
    }

    public ReadOnlyObjectProperty<Object> currentWorkspaceProperty() {
        return currentWorkspaceProperty;
    }

    public ObservableList<StatusItemViewModel> statusItems() {
        return statusItems;
    }
}
