package mvvm.example.shell.main.sidebar;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import mvvm.example.core.view.controls.Buttons;

public class SidebarView extends BorderPane {

    private final VBox navigationHost = navigationHost();

    public SidebarView(SidebarViewModel viewModel) {
        setRight(separator());
        setCenter(navigationHost);
        setPrefWidth(180);

        setContent(viewModel.navigationItems());

        viewModel
            .navigationItems()
            .addListener((InvalidationListener) _ -> setContent(viewModel.navigationItems()));
    }

    private void setContent(ObservableList<SidebarItemViewModel> items) {
        navigationHost.getChildren().setAll(
            items.stream().map(SidebarView::navigationButton).toList()
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

    private static Button navigationButton(SidebarItemViewModel viewModel) {
        var button = Buttons.button(viewModel.titleProperty(), viewModel.openWorkspaceAction());
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }
}
