package mvvm.example.shell.main;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import mvvm.example.core.view.controls.Spacer;

public class StatusBarView extends BorderPane {

    public StatusBarView(ReadOnlyStringProperty statusText) {
        var content = new HBox();
        content.setPadding(new Insets(2, 8, 2, 8));

        var statusLabel = new Label();
        statusLabel.textProperty().bind(statusText);

        content.getChildren().addAll(Spacer.create(), statusLabel);

        setTop(new Separator());
        setCenter(content);
    }
}
