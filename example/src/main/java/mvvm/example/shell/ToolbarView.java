package mvvm.example.shell;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Control;
import javafx.scene.control.ToolBar;
import mvvm.example.core.view.controls.Buttons;

import java.util.List;
import java.util.stream.Stream;

public class ToolbarView extends ToolBar {

    private final ChangeListener<WorkspaceTabViewModel> onSelectedTabChanged = (_, _, _) -> rebuild();

    private WorkspaceViewModel workspace;

    public ToolbarView(ShellViewModel shell) {
        setWorkspace(shell.currentWorkspaceProperty().get());
        shell.currentWorkspaceProperty().addListener((_, _, newWorkspace) -> setWorkspace(newWorkspace));
    }

    private void setWorkspace(WorkspaceViewModel newWorkspace) {
        if (workspace != null) {
            workspace.selectedTabProperty().removeListener(onSelectedTabChanged);
        }

        workspace = newWorkspace;

        if (workspace != null) {
            workspace.selectedTabProperty().addListener(onSelectedTabChanged);
        }

        rebuild();
    }

    private void rebuild() {
        if (workspace == null) {
            getItems().clear();
            return;
        }

        var selectedTab = workspace.selectedTabProperty().get();
        var tabActions = selectedTab != null ? selectedTab.content().toolbarActions() : List.<ToolbarItem>of();

        getItems().setAll(
            Stream.concat(workspace.toolbarActions().stream(), tabActions.stream())
                .map(ToolbarView::button)
                .toList()
        );
    }

    private static Control button(ToolbarItem item) {
        return switch (item) {
            case ToolbarItem.Sync sync -> Buttons.button(sync.label(), sync.action());
            case ToolbarItem.Async async -> Buttons.button(async.label(), async.action());
        };
    }
}
