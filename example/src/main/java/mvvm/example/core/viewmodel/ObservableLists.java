package mvvm.example.core.viewmodel;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class ObservableLists {

    public static <K, V> ObservableList<V> valuesOf(ObservableMap<K, V> map) {
        var values = FXCollections.observableArrayList(map.values());

        map.addListener((MapChangeListener<K, V>) change -> {
            if (change.wasAdded()) {
                values.add(change.getValueAdded());
            }
            if (change.wasRemoved()) {
                values.remove(change.getValueRemoved());
            }
        });

        return values;
    }

    private ObservableLists() {}
}
