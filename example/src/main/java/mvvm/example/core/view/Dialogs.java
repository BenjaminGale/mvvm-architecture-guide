package mvvm.example.core.view;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Region;
import mvvm.example.core.viewmodel.Action;

public class Dialogs {

    public static Dialog<Runnable> create(String title, Region content, String confirmLabel, Action onConfirmAction) {
        var confirmBtnType = new ButtonType(confirmLabel, ButtonBar.ButtonData.OK_DONE);

        var dialog = new Dialog<Runnable>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtnType, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == confirmBtnType ? onConfirmAction::execute : null);

        var confirmBtn = (Button) dialog.getDialogPane().lookupButton(confirmBtnType);
        confirmBtn.isDefaultButton();
        confirmBtn.disableProperty().bind(onConfirmAction.canExecuteProperty().not());

        return dialog;
    }

    private Dialogs() {}
}
