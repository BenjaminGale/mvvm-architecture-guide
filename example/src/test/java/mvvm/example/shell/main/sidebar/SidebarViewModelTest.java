package mvvm.example.shell.main.sidebar;

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

@DisplayName("SidebarViewModel")
class SidebarViewModelTest {

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it shows the expected navigation items")
        void exposesNavigationItems() {
            var items = FXCollections.observableArrayList(
                new SidebarItemViewModel("Orders", () -> {}),
                new SidebarItemViewModel("Customers", () -> {})
            );

            var vm = new SidebarViewModel(items);

            assertEquals(items, vm.navigationItems());
        }
    }

    @Nested
    @DisplayName("when the navigation items change")
    class WhenSourceChanges {

        static Stream<Arguments> sourceMutationCases() {
            return Stream.of(
                Arguments.of(
                    "after an item is added",
                    (Consumer<ObservableList<SidebarItemViewModel>>) list ->
                        list.add(new SidebarItemViewModel("Reports", () -> {})),
                    List.of("Orders", "Customers", "Reports")
                ),
                Arguments.of(
                    "after an item is removed",
                    (Consumer<ObservableList<SidebarItemViewModel>>) List::removeFirst,
                    List.of("Customers")
                ),
                Arguments.of(
                    "after the list is cleared",
                    (Consumer<ObservableList<SidebarItemViewModel>>) ObservableList::clear,
                    List.of()
                )
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("sourceMutationCases")
        @DisplayName("it shows the expected navigation items")
        void reflectsChange(String caseName, Consumer<ObservableList<SidebarItemViewModel>> mutate, List<String> expectedTitles) {
            var items = FXCollections.observableArrayList(
                new SidebarItemViewModel("Orders", () -> {}),
                new SidebarItemViewModel("Customers", () -> {})
            );
            var vm = new SidebarViewModel(items);

            mutate.accept(items);

            var actualTitles = vm.navigationItems().stream()
                .map(item -> item.titleProperty().get())
                .toList();

            assertEquals(expectedTitles, actualTitles);
        }
    }
}
