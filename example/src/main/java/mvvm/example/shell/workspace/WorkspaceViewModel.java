package mvvm.example.shell.workspace;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.ObservableLists;
import mvvm.example.shell.toolbar.ToolbarItem;
import mvvm.example.shell.tabs.TabViewModel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public class WorkspaceViewModel {

    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title");
    private final ObservableMap<Object, TabViewModel> tabsByKey = FXCollections.observableMap(new LinkedHashMap<>());
    private final ObservableList<TabViewModel> tabs = ObservableLists.valuesOf(tabsByKey);
    private final ObjectProperty<TabViewModel> selectedTab = new SimpleObjectProperty<>(this, "selectedTab");
    private Action.Listener onOpen = () -> {};
    private final Action openAction = new Action(() -> onOpen.actionExecuted());
    private List<ToolbarItem> toolbarActions = List.of();

    public WorkspaceViewModel(String title) {
        this.title.set(title);
    }

    public ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    public WorkspaceViewModel withToolbarActions(List<ToolbarItem> toolbarActions) {
        this.toolbarActions = toolbarActions;
        return this;
    }

    public List<ToolbarItem> toolbarActions() {
        return toolbarActions;
    }

    public ObservableList<TabViewModel> tabs() {
        return tabs;
    }

    public ObjectProperty<TabViewModel> selectedTabProperty() {
        return selectedTab;
    }

    public Action openAction() {
        return openAction;
    }

    public void onOpen(Action.Listener listener) {
        this.onOpen = listener;
    }

    public void openTab(Object key, Supplier<TabViewModel> tabFactory) {
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

    private void removeTab(Object key, TabViewModel tab) {
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
