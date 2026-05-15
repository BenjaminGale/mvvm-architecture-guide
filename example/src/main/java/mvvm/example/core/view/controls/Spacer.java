package mvvm.example.core.view.controls;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class Spacer extends Region {

    public static Region create() {
        return new Spacer();
    }

    private Spacer() {
        HBox.setHgrow(this, Priority.ALWAYS);
    }
}
