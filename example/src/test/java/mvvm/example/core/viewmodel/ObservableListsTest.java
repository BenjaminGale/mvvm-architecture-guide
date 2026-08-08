package mvvm.example.core.viewmodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ViewModel.ObservableLists")
class ObservableListsTest {

    private static ObservableMap<String, String> map(String... entries) {
        ObservableMap<String, String> map = FXCollections.observableMap(new LinkedHashMap<>());
        for (int i = 0; i < entries.length; i += 2) {
            map.put(entries[i], entries[i + 1]);
        }
        return map;
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it reflects the map's current values")
        void reflectsCurrentValues() {
            var map = map("a", "Apple", "b", "Banana");

            var values = ObservableLists.valuesOf(map);

            assertEquals(List.of("Apple", "Banana"), values);
        }
    }

    @Nested
    @DisplayName("when an entry is added to the map")
    class WhenEntryAdded {

        @Test
        @DisplayName("the value appears in the list")
        void valueAppears() {
            var map = map("a", "Apple");
            var values = ObservableLists.valuesOf(map);

            map.put("b", "Banana");

            assertEquals(List.of("Apple", "Banana"), values);
        }
    }

    @Nested
    @DisplayName("when an entry is removed from the map")
    class WhenEntryRemoved {

        @Test
        @DisplayName("the value disappears from the list")
        void valueDisappears() {
            var map = map("a", "Apple", "b", "Banana");
            var values = ObservableLists.valuesOf(map);

            map.remove("a");

            assertEquals(List.of("Banana"), values);
        }
    }
}
