package mvvm.example.shell.main.sidebar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Shell.SidebarItemViewModel")
class SidebarItemViewModelTest {

    @Nested
    @DisplayName("when created")
    class WhenCreated {

        @ParameterizedTest(name = "with title \"{0}\"")
        @ValueSource(strings = {"Orders", "Stock", "Customers"})
        @DisplayName("it has the expected title")
        void showsTitle(String title) {
            var vm = new SidebarItemViewModel(title, () -> {});

            assertEquals(title, vm.titleProperty().get());
        }

        @Test
        @DisplayName("its action is enabled")
        void actionIsExecutable() {
            var vm = new SidebarItemViewModel("Orders", () -> {});

            assertTrue(vm.action().canExecute());
        }
    }

    @Nested
    @DisplayName("when the action is executed")
    class WhenExecuted {

        @Test
        @DisplayName("its configured behaviour runs")
        void invokesListener() {
            var invoked = new boolean[]{false};
            var vm = new SidebarItemViewModel("Orders", () -> invoked[0] = true);

            vm.action().execute();

            assertTrue(invoked[0]);
        }
    }
}
