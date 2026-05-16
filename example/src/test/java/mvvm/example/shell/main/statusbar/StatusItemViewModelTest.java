package mvvm.example.shell.main.statusbar;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatusItemViewModel")
class StatusItemViewModelTest {

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it has the expected label")
        void hasExpectedLabel() {
            var count = new ReadOnlyIntegerWrapper(0);
            var vm = new StatusItemViewModel(count.getReadOnlyProperty(), LabelType.All_ORDERS);

            assertEquals(LabelType.All_ORDERS, vm.label());
        }

        @Test
        @DisplayName("it shows the current count")
        void showsCurrentCount() {
            var count = new ReadOnlyIntegerWrapper(5);
            var vm = new StatusItemViewModel(count.getReadOnlyProperty(), LabelType.All_ORDERS);

            assertEquals(5, vm.countProperty().get());
        }
    }

    @Nested
    @DisplayName("when the count changes")
    class WhenCountChanges {

        @Test
        @DisplayName("it shows the updated count")
        void showsUpdatedCount() {
            var count = new ReadOnlyIntegerWrapper(0);
            var vm = new StatusItemViewModel(count.getReadOnlyProperty(), LabelType.All_ORDERS);

            count.set(7);

            assertEquals(7, vm.countProperty().get());
        }
    }
}
