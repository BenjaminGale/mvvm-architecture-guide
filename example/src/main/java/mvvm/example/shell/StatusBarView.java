package mvvm.example.shell;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.controls.Spacer;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;

import java.util.List;

public class StatusBarView extends BorderPane {

    private final ViewLocator<Region> viewLocator;
    private final HBox content = content();
    private final ChangeListener<WorkspaceTabViewModel> onSelectedTabChanged = (_, _, _) -> rebuild();

    private WorkspaceViewModel workspace;

    public StatusBarView(ShellViewModel shell, ViewLocator<Region> viewLocator) {
        this.viewLocator = viewLocator;

        setTop(new Separator());
        setCenter(content);

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
        var selectedTab = workspace != null ? workspace.selectedTabProperty().get() : null;
        var statusItems = selectedTab != null ? selectedTab.content().statusItems() : List.<StatusItemViewModel>of();

        var children = content.getChildren();
        children.clear();
        children.add(Spacer.create());
        for (int i = 0; i < statusItems.size(); i++) {
            if (i > 0) children.add(new Separator(Orientation.VERTICAL));
            children.add(viewLocator.locate(statusItems.get(i)));
        }
    }

    private static HBox content() {
        var content = new HBox();
        content.setPadding(new Insets(5, 8, 5, 8));
        content.setMinHeight(24);
        content.setSpacing(8);
        return content;
    }
}
