package mvvm.example.shell;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mvvm.example.core.view.ViewRouter;
import mvvm.example.orders.editor.edititem.EditItemView;

public class DialogManagerView extends Pane {

    private Stage currentDialog;

    public DialogManagerView(ViewRouter viewRouter) {
        setMouseTransparent(true);
        setPickOnBounds(false);

        viewRouter.addListener(EditItemView.class, this::openAsDialog);
    }

    private void openAsDialog(Region view) {
        closeCurrentDialog();
        currentDialog = new Stage();
        currentDialog.initModality(Modality.APPLICATION_MODAL);
        currentDialog.initOwner(getScene().getWindow());
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
