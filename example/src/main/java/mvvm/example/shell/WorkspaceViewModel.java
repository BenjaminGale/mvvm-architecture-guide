package mvvm.example.shell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.core.viewmodel.Action;

public class WorkspaceViewModel {

    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title");
    private final ObservableList<WorkspaceTab> tabs = FXCollections.observableArrayList();
    private final ObjectProperty<WorkspaceTab> selectedTab = new SimpleObjectProperty<>(this, "selectedTab");
    private Action.Listener onOpen = () -> {};
    private final Action openAction = new Action(() -> onOpen.actionExecuted());

    public WorkspaceViewModel(String title, WorkspaceTab explorerTab) {
        this.title.set(title);
        this.tabs.add(explorerTab);
        this.selectedTab.set(explorerTab);
    }

    public ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    public ObservableList<WorkspaceTab> tabs() {
        return tabs;
    }

    public ObjectProperty<WorkspaceTab> selectedTabProperty() {
        return selectedTab;
    }

    public Action openAction() {
        return openAction;
    }

    public void onOpen(Action.Listener listener) {
        this.onOpen = listener;
    }

    public void openTab(WorkspaceTab tab) {
        if (!tabs.contains(tab)) {
            tabs.add(tab);
            tab.onClose(() -> removeTab(tab));
        }
        selectedTab.set(tab);
    }

    private void removeTab(WorkspaceTab tab) {
        int index = tabs.indexOf(tab);
        if (index < 0) {
            throw new IllegalStateException("Tab is not open in this workspace");
        }

        tabs.remove(index);

        if (selectedTab.get() == tab) {
            selectedTab.set(tabs.get(Math.min(index, tabs.size() - 1)));
        }
    }
}
