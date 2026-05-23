package mvvm.example.core.view.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Function;

public class SelectorView<T> extends VBox {

    public static <T> Dialog<Runnable> dialog(
            String title,
            String searchPrompt,
            StringProperty searchText,
            ObservableList<T> items,
            ObjectProperty<T> selection,
            Function<T, String> displayFn,
            Runnable onConfirm) {

        var selectBtn = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        var dialog = new Dialog<Runnable>();

        dialog.setTitle(title);
        dialog.getDialogPane().setContent(new SelectorView<>(searchPrompt, searchText, items, selection, displayFn));
        dialog.getDialogPane().setPrefWidth(380);
        dialog.getDialogPane().getButtonTypes().addAll(selectBtn, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == selectBtn ? onConfirm : null);

        var okButton = (Button) dialog.getDialogPane().lookupButton(selectBtn);
        okButton.disableProperty().bind(selection.isNull());

        return dialog;
    }

    private SelectorView(
            String searchPrompt,
            StringProperty searchText,
            ObservableList<T> items,
            ObjectProperty<T> selection,
            Function<T, String> displayFn) {

        setSpacing(8);
        setPadding(new Insets(8));
        setPrefHeight(280);

        var searchField = new TextField();
        searchField.setPromptText(searchPrompt);
        searchField.textProperty().bindBidirectional(searchText);

        var list = new ListView<T>();
        list.setItems(items);
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayFn.apply(item));
            }
        });

        list.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) selection.set(val);
        });

        var current = selection.get();
        if (current != null) {
            list.getSelectionModel().select(current);
            list.scrollTo(current);
        }

        VBox.setVgrow(list, Priority.ALWAYS);
        getChildren().addAll(searchField, list);

        Controls.focusOnShow(searchField);
    }
}
