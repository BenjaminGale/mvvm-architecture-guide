package mvvm.example.shell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.ObservableLists;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class WorkspaceViewModel {

    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title");
    private final ObservableMap<Object, WorkspaceTabViewModel> tabsByKey = FXCollections.observableMap(new LinkedHashMap<>());
    private final ObservableList<WorkspaceTabViewModel> tabs = ObservableLists.valuesOf(tabsByKey);
    private final ObjectProperty<WorkspaceTabViewModel> selectedTab = new SimpleObjectProperty<>(this, "selectedTab");
    private Action.Listener onOpen = () -> {};
    private final Action openAction = new Action(() -> onOpen.actionExecuted());

    public WorkspaceViewModel(String title) {
        this.title.set(title);
    }

    public ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    public ObservableList<WorkspaceTabViewModel> tabs() {
        return tabs;
    }

    public ObjectProperty<WorkspaceTabViewModel> selectedTabProperty() {
        return selectedTab;
    }

    public Action openAction() {
        return openAction;
    }

    public void onOpen(Action.Listener listener) {
        this.onOpen = listener;
    }

    public void openTab(Object key, Supplier<WorkspaceTabViewModel> tabFactory) {
        var existing = tabsByKey.get(key);
        if (existing != null) {
            selectedTab.set(existing);
            return;
        }

        var tab = tabFactory.get();
        tabsByKey.put(key, tab);
        tab.onClose(() -> removeTab(key, tab));
        selectedTab.set(tab);
    }

    private void removeTab(Object key, WorkspaceTabViewModel tab) {
        if (!tabsByKey.containsKey(key)) {
            throw new IllegalStateException("Tab is not open in this workspace");
        }

        int index = tabs.indexOf(tab);
        tabsByKey.remove(key);

        if (selectedTab.get() == tab) {
            selectedTab.set(tabs.isEmpty() ? null : tabs.get(Math.min(index, tabs.size() - 1)));
        }
    }
}
