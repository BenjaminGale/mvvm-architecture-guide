package mvvm.example.core.view;

import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import mvvm.example.core.viewmodel.AppHost;

public class DialogManager {

    private final Window owner;
    private final ViewLocator viewLocator;
    private final AppHost host;
    private Stage currentDialog;

    public DialogManager(Window owner, ViewLocator viewLocator, AppHost host) {
        this.owner      = owner;
        this.viewLocator = viewLocator;
        this.host        = host;
    }

    public <VM> void register(Class<VM> vmClass) {
        host.receive(vmClass, vm -> openAsDialog(viewLocator.resolve(vm)));
    }

    private void openAsDialog(Region view) {
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
