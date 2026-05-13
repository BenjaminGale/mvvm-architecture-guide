package mvvm.example.core.view;

import javafx.stage.Modality;
import javafx.stage.Window;
import mvvm.example.core.viewmodel.AppHost;

public class DialogManager {

    private final Window owner;
    private final ViewLocator viewLocator;
    private final AppHost host;

    public DialogManager(Window owner, ViewLocator viewLocator, AppHost host) {
        this.owner       = owner;
        this.viewLocator = viewLocator;
        this.host        = host;
    }

    public <VM> void register(Class<VM> vmClass) {
        host.receive(vmClass, vm -> {
            var dialog = viewLocator.locateDialog(vm);
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait().ifPresent(Runnable::run);
        });
    }
}
