package mvvm.example.core.view.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Function;

public class SelectorView<T> extends VBox {

    public SelectorView(
            String searchPrompt,
            StringProperty searchText,
            ObservableList<T> items,
            ObjectProperty<T> selection,
            Function<T, String> displayFn) {

        setSpacing(8);
        setPadding(new Insets(8));
        setPrefWidth(380);
        setPrefHeight(280);

        getChildren().addAll(
            searchField(searchPrompt, searchText),
            listView(items, selection, displayFn)
        );
    }

    private static TextField searchField(String prompt, StringProperty searchText) {
        var field = new TextField();
        field.setPromptText(prompt);
        field.textProperty().bindBidirectional(searchText);
        Controls.focusOnShow(field);
        return field;
    }

    private static <T> ListView<T> listView(ObservableList<T> items, ObjectProperty<T> selection, Function<T, String> displayFn) {
        var list = new ListView<T>();
        VBox.setVgrow(list, Priority.ALWAYS);
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
        return list;
    }
}
