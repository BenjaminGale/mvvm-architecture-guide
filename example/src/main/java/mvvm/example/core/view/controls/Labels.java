package mvvm.example.core.view.controls;

import javafx.beans.binding.IntegerExpression;
import javafx.scene.control.Label;

public class Labels {

    public static Label badge(IntegerExpression count) {
        var badge = new Label();
        badge.textProperty().bind(count.asString());
        badge.setStyle("""
            -fx-background-color: #e05252;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-padding: 1 5 1 5;
            -fx-font-size: 10;
            -fx-font-weight: bold;
            """);
        badge.visibleProperty().bind(count.greaterThan(0));
        return badge;
    }

    private Labels() {}
}
