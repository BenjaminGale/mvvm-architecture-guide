package mvvm.example.shell.main.statusbar;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.controls.Spacer;

import java.util.ArrayList;
import java.util.List;

public class StatusBarView extends BorderPane {

    private final HBox content = new HBox();

    public StatusBarView(ObservableList<StatusItemViewModel> statusItems, ViewLocator<Region> viewLocator) {
        content.setPadding(new Insets(5, 8, 5, 8));
        content.setMinHeight(24);
        content.setSpacing(8);

        setTop(new Separator());
        setCenter(content);

        rebuild(statusItems, viewLocator);
        statusItems.addListener((ListChangeListener<StatusItemViewModel>) _ -> rebuild(statusItems, viewLocator));
    }

    private void rebuild(ObservableList<StatusItemViewModel> statusItems, ViewLocator<Region> viewLocator) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(Spacer.create());

        for (int i = 0; i < statusItems.size(); i++) {
            if (i > 0) nodes.add(new Separator(Orientation.VERTICAL));
            nodes.add(viewLocator.locate(statusItems.get(i)));
        }

        content.getChildren().setAll(nodes);
    }
}
