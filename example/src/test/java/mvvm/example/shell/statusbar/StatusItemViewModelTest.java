package mvvm.example.shell.statusbar;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shell.StatusItemViewModel")
class StatusItemViewModelTest {

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @ParameterizedTest(name = "with label {0}")
        @EnumSource(LabelType.class)
        @DisplayName("it has the expected label")
        void hasExpectedLabel(LabelType label) {
            var count = new ReadOnlyIntegerWrapper(0);
            var vm = new StatusItemViewModel(count.getReadOnlyProperty(), label);

            assertEquals(label, vm.label());
        }

        @ParameterizedTest(name = "with count {0}")
        @ValueSource(ints = {0, 5, 42})
        @DisplayName("it shows the current count")
        void showsCurrentCount(int initialCount) {
            var count = new ReadOnlyIntegerWrapper(initialCount);
            var vm = new StatusItemViewModel(count.getReadOnlyProperty(), LabelType.All_ORDERS);

            assertEquals(initialCount, vm.countProperty().get());
        }
    }

    @Nested
    @DisplayName("when the count changes")
    class WhenCountChanges {

        @ParameterizedTest(name = "from {0} to {1}")
        @CsvSource({"0, 7", "5, 42", "10, 1"})
        @DisplayName("it shows the updated count")
        void showsUpdatedCount(int initialCount, int updatedCount) {
            var count = new ReadOnlyIntegerWrapper(initialCount);
            var vm = new StatusItemViewModel(count.getReadOnlyProperty(), LabelType.All_ORDERS);

            count.set(updatedCount);

            assertEquals(updatedCount, vm.countProperty().get());
        }
    }
}
