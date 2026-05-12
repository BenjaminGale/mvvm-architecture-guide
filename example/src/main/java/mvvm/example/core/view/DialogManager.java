package mvvm.example.core.view;

import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class DialogManager {

    private final Window owner;
    private Stage currentDialog;

    public DialogManager(Window owner) {
        this.owner = owner;
    }

    public void openAsDialog(Region view) {
        closeCurrentDialog();
        currentDialog = new Stage();
        currentDialog.initModality(Modality.APPLICATION_MODAL);
        currentDialog.initOwner(owner);
        currentDialog.setScene(new Scene(view));
        currentDialog.show();
    }

    private void closeCurrentDialog() {
        if (currentDialog != null) {
            currentDialog.close();
            currentDialog = null;
        }
    }
}
