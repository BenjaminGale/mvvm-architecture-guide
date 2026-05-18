package mvvm.example.core.viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ExplorerViewModel<T> {

    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final ObjectProperty<T> selectedItem = new SimpleObjectProperty<>(this, "selectedItem");

    private final AsyncAction fetchItemsAction;
    private final Action addItemAction;
    private final Action editItemAction;
    private final Action deleteItemAction;

    protected ExplorerViewModel() {
        this.fetchItemsAction = new AsyncAction(
            () -> fetchItemsAsync().thenApply(results -> () -> items.setAll(results))
        );

        var canEdit = selectedItem.isNotNull().and(canEditCondition());
        var canDelete = selectedItem.isNotNull().and(canDeleteCondition());

        this.addItemAction = new Action(this::addItem, canAddCondition());
        this.editItemAction = new Action(() -> editItem(selectedItem.get()), canEdit);
        this.deleteItemAction = new Action(() -> deleteItem(selectedItem.get()), canDelete);
    }

    public ObservableList<T> items() {
        return items;
    }

    public ObjectProperty<T> selectedItemProperty() {
        return selectedItem;
    }

    public AsyncAction fetchItemsAction() {
        return fetchItemsAction;
    }

    public Action addItemAction() {
        return addItemAction;
    }

    public Action editItemAction() {
        return editItemAction;
    }

    public Action deleteItemAction() {
        return deleteItemAction;
    }

    protected ObservableBooleanValue canAddCondition() {
        return new SimpleBooleanProperty(true);
    }

    protected ObservableBooleanValue canEditCondition() {
        return new SimpleBooleanProperty(true);
    }

    protected ObservableBooleanValue canDeleteCondition() {
        return new SimpleBooleanProperty(true);
    }

    protected abstract CompletableFuture<List<T>> fetchItemsAsync();

    protected abstract void addItem();

    protected abstract void editItem(T item);

    protected abstract void deleteItem(T item);
}
