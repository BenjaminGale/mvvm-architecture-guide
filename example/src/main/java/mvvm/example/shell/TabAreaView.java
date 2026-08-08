package mvvm.example.shell;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;
import mvvm.example.core.view.ViewLocator;

public class TabAreaView extends TabPane {

    private final ViewLocator<Region> viewLocator;
    private final ListChangeListener<WorkspaceTabViewModel> onTabsChanged = this::applyTabsChange;
    private final ChangeListener<WorkspaceTabViewModel> onSelectedTabChanged = (_, _, tab) -> selectTab(tab);

    private WorkspaceViewModel workspace;

    public TabAreaView(ShellViewModel shell, ViewLocator<Region> viewLocator) {
        this.viewLocator = viewLocator;

        setTabClosingPolicy(TabClosingPolicy.ALL_TABS);

        getSelectionModel().selectedItemProperty().addListener((_, _, tab) -> {
            if (workspace != null && tab != null) {
                workspace.selectedTabProperty().set((WorkspaceTabViewModel) tab.getUserData());
            }
        });

        setWorkspace(shell.currentWorkspaceProperty().get());
        shell.currentWorkspaceProperty().addListener((_, _, newWorkspace) -> setWorkspace(newWorkspace));
    }

    private void setWorkspace(WorkspaceViewModel newWorkspace) {
        if (workspace != null) {
            workspace.tabs().removeListener(onTabsChanged);
            workspace.selectedTabProperty().removeListener(onSelectedTabChanged);
        }

        workspace = newWorkspace;
        getTabs().clear();

        if (workspace == null) return;

        var tabToSelect = workspace.selectedTabProperty().get();
        getTabs().setAll(workspace.tabs().stream().map(this::toJavaFxTab).toList());
        workspace.tabs().addListener(onTabsChanged);
        workspace.selectedTabProperty().addListener(onSelectedTabChanged);
        selectTab(tabToSelect);
    }

    private void applyTabsChange(ListChangeListener.Change<? extends WorkspaceTabViewModel> change) {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.getRemoved().forEach(removed ->
                    getTabs().removeIf(javaFxTab -> javaFxTab.getUserData() == removed));
            }
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(added -> getTabs().add(toJavaFxTab(added)));
            }
        }
        selectTab(workspace.selectedTabProperty().get());
    }

    private void selectTab(WorkspaceTabViewModel tab) {
        getTabs().stream()
            .filter(javaFxTab -> javaFxTab.getUserData() == tab)
            .findFirst()
            .ifPresent(javaFxTab -> getSelectionModel().select(javaFxTab));
    }

    private Tab toJavaFxTab(WorkspaceTabViewModel tab) {
        var javaFxTab = new Tab();
        javaFxTab.setUserData(tab);
        javaFxTab.textProperty().bind(tab.titleProperty());
        javaFxTab.setClosable(tab.closeAction().canExecute());
        javaFxTab.setContent(viewLocator.locate(tab.content().viewModel()));
        javaFxTab.setOnCloseRequest(event -> {
            event.consume();
            tab.closeAction().execute();
        });
        return javaFxTab;
    }
}
