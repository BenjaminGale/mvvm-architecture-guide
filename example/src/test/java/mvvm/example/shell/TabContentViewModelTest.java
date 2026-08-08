package mvvm.example.shell;

import mvvm.example.core.viewmodel.Action;
import mvvm.example.core.viewmodel.AsyncAction;
import mvvm.example.shell.main.statusbar.LabelType;
import mvvm.example.shell.main.statusbar.StatusItemViewModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javafx.beans.property.ReadOnlyIntegerWrapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shell.TabContentViewModel")
class TabContentViewModelTest {

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @Test
        @DisplayName("it exposes the given view model")
        void exposesViewModel() {
            var viewModel = new Object();

            var content = new TabContentViewModel(viewModel);

            assertEquals(viewModel, content.viewModel());
        }

        @Test
        @DisplayName("it has no toolbar actions")
        void hasNoToolbarActions() {
            var content = new TabContentViewModel(new Object());

            assertEquals(List.of(), content.toolbarActions());
        }

        @Test
        @DisplayName("it has no status items")
        void hasNoStatusItems() {
            var content = new TabContentViewModel(new Object());

            assertEquals(List.of(), content.statusItems());
        }
    }

    @Nested
    @DisplayName("when configured with toolbar actions")
    class WhenConfiguredWithToolbarActions {

        @Test
        @DisplayName("it exposes them")
        void exposesToolbarActions() {
            var toolbarActions = List.<ToolbarItem>of(
                new ToolbarItem.Sync("Save", new Action(() -> {})),
                new ToolbarItem.Async("Refresh", new AsyncAction(() -> null))
            );

            var content = new TabContentViewModel(new Object()).withToolbarActions(toolbarActions);

            assertEquals(toolbarActions, content.toolbarActions());
        }
    }

    @Nested
    @DisplayName("when configured with status items")
    class WhenConfiguredWithStatusItems {

        @Test
        @DisplayName("it exposes them")
        void exposesStatusItems() {
            var statusItems = List.of(
                new StatusItemViewModel(new ReadOnlyIntegerWrapper(3).getReadOnlyProperty(), LabelType.All_ORDERS)
            );

            var content = new TabContentViewModel(new Object()).withStatusItems(statusItems);

            assertEquals(statusItems, content.statusItems());
        }
    }
}
