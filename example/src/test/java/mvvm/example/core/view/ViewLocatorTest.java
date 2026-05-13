package mvvm.example.core.view;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ViewLocator")
class ViewLocatorTest {

    private static class StubViewModel {}
    private static class StubView extends Pane {}
    private static class OtherViewModel {}

    @Nested
    @DisplayName("when a view is registered for a ViewModel")
    class WhenRegistered {

        @Test
        @DisplayName("the expected view is returned when resolving the registered viewModel")
        void resolvesCorrectViewType() {
            var locator = new ViewLocator();
            locator.register(StubViewModel.class, vm -> new StubView());

            Region view = locator.resolve(new StubViewModel());

            assertInstanceOf(StubView.class, view);
        }
    }

    @Nested
    @DisplayName("when a view is not registered for a viewModel")
    class WhenNotRegistered {

        @Test
        @DisplayName("an exception is thrown when resolving the unregistered viewModel")
        void throwsForUnregisteredViewModel() {
            var locator = new ViewLocator();

            assertThrows(IllegalStateException.class,
                () -> locator.resolve(new OtherViewModel()));
        }
    }
}
