package mvvm.example.shell.main.statusbar;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.controls.Spacer;

public class StatusBarView extends BorderPane {

    private final ViewLocator<Region> viewLocator;
    private final HBox content = content();

    public StatusBarView(StatusBarViewModel viewModel, ViewLocator<Region> viewLocator) {
        this.viewLocator = viewLocator;

        setTop(new Separator());
        setCenter(content);

        setContent(viewModel.statusItems());

        viewModel.statusItems()
            .addListener((ListChangeListener<StatusItemViewModel>) _ ->
                setContent(viewModel.statusItems()));
    }

    private void setContent(ObservableList<StatusItemViewModel> statusItems) {
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
