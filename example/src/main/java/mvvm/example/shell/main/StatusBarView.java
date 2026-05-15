package mvvm.example.shell.main;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import mvvm.example.core.view.controls.Spacer;

import java.util.ArrayList;
import java.util.List;

public class StatusBarView extends BorderPane {

    private final HBox content = new HBox();

    public StatusBarView(ObservableList<ReadOnlyStringProperty> statusMessages) {
        content.setPadding(new Insets(5, 8, 5, 8));
        content.setMinHeight(24);
        content.setSpacing(8);

        setTop(new Separator());
        setCenter(content);

        rebuild(statusMessages);
        statusMessages.addListener((ListChangeListener<ReadOnlyStringProperty>) _ -> rebuild(statusMessages));
    }

    private void rebuild(ObservableList<ReadOnlyStringProperty> statusMessages) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(Spacer.create());

        for (int i = 0; i < statusMessages.size(); i++) {
            if (i > 0) nodes.add(new Separator(Orientation.VERTICAL));
            var label = new Label();
            label.textProperty().bind(statusMessages.get(i));
            nodes.add(label);
        }

        content.getChildren().setAll(nodes);
    }
}
