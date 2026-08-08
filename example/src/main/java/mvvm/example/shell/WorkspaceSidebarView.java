package mvvm.example.shell;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mvvm.example.core.view.controls.Buttons;

public class WorkspaceSidebarView extends BorderPane {

    private final VBox navigationHost = navigationHost();

    public WorkspaceSidebarView(ShellViewModel viewModel) {
        setRight(separator());
        setCenter(navigationHost);
        setPrefWidth(180);

        setContent(viewModel);

        viewModel.workspaces().addListener((InvalidationListener) _ -> setContent(viewModel));
    }

    private void setContent(ShellViewModel viewModel) {
        navigationHost.getChildren().setAll(
            viewModel.workspaces().stream().map(WorkspaceSidebarView::navigationButton).toList()
        );
    }

    private static VBox navigationHost() {
        var host = new VBox();
        host.setPadding(new Insets(8));
        host.setSpacing(4);
        return host;
    }

    private static Region separator() {
        var separator = new Region();
        separator.setStyle("-fx-background-color: -fx-box-border;");
        separator.setPrefWidth(1);
        separator.setMinWidth(1);
        separator.setMaxWidth(1);
        return separator;
    }

    private static Button navigationButton(WorkspaceViewModel workspace) {
        var button = Buttons.button(workspace.titleProperty(), workspace.openAction());
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }
}
