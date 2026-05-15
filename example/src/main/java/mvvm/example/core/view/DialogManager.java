package mvvm.example.core.view;

import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.function.Function;

public class DialogManager {

    private final Window owner;
    private final ViewLocator<Dialog<Runnable>> viewLocator;

    public DialogManager(Window owner, ViewLocator<Dialog<Runnable>> viewLocator) {
        this.owner = owner;
        this.viewLocator = viewLocator;
    }

    public <TViewModel> void register(Class<TViewModel> vmClass, Function<TViewModel, Dialog<Runnable>> viewFactory) {
        viewLocator.register(vmClass, viewFactory);
    }

    public void show(Object viewModel) {
        var dialog = viewLocator.locate(viewModel);
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait().ifPresent(Runnable::run);
    }
}
