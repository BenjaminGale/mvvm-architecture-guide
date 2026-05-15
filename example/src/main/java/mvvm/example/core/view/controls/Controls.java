package mvvm.example.core.view.controls;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;

public class Controls {

    public static void focusOnShow(Node node) {
        node.sceneProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Scene> obs, Scene old, Scene scene) {
                if (scene != null) {
                    node.requestFocus();
                    node.sceneProperty().removeListener(this);
                }
            }
        });
    }

    private Controls() {}
}
