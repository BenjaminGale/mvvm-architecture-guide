package mvvm.example.shell.main.statusbar;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatusBarViewModel")
class StatusBarViewModelTest {

    private static StatusItemViewModel item(LabelType label) {
        return new StatusItemViewModel(new ReadOnlyIntegerWrapper(0).getReadOnlyProperty(), label);
    }

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it shows the expected status items")
        void showsStatusItems() {
            var items = FXCollections.observableArrayList(
                item(LabelType.All_ORDERS),
                item(LabelType.OVERDUE_ORDERS)
            );

            var vm = new StatusBarViewModel(items);

            assertEquals(items, vm.statusItems());
        }
    }

    @Nested
    @DisplayName("when the status items change")
    class WhenStatusItemsChange {

        static Stream<Arguments> sourceMutationCases() {
            return Stream.of(
                Arguments.of(
                    "after an item is added",
                    (Consumer<ObservableList<StatusItemViewModel>>) list ->
                        list.add(item(LabelType.OVERDUE_ORDERS)),
                    List.of(LabelType.All_ORDERS, LabelType.OVERDUE_ORDERS, LabelType.OVERDUE_ORDERS)
                ),
                Arguments.of(
                    "after an item is removed",
                    (Consumer<ObservableList<StatusItemViewModel>>) List::removeFirst,
                    List.of(LabelType.OVERDUE_ORDERS)
                ),
                Arguments.of(
                    "after the list is cleared",
                    (Consumer<ObservableList<StatusItemViewModel>>) ObservableList::clear,
                    List.of()
                )
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("sourceMutationCases")
        @DisplayName("it shows the expected status items")
        void reflectsChange(String caseName, Consumer<ObservableList<StatusItemViewModel>> mutate, List<LabelType> expectedLabels) {
            var items = FXCollections.observableArrayList(
                item(LabelType.All_ORDERS),
                item(LabelType.OVERDUE_ORDERS)
            );
            var vm = new StatusBarViewModel(items);

            mutate.accept(items);

            var actualLabels = vm.statusItems().stream()
                .map(StatusItemViewModel::label)
                .toList();

            assertEquals(expectedLabels, actualLabels);
        }
    }
}
