package mvvm.example.core.view;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("View.ViewLocator")
class ViewLocatorTest {

    private static class StubViewModelA {}
    private static class StubViewModelB {}
    private static class StubViewModelC {}
    private static class StubViewA extends Pane {}
    private static class StubViewB extends Pane {}

    @Nested
    @DisplayName("when the locator has no registrations")
    class WhenEmpty {

        @Test
        @DisplayName("an exception is thrown when locating a viewModel")
        void throwsWhenLocatingAnyViewModel() {
            var locator = new ViewLocator<Region>();

            assertThrows(IllegalStateException.class, () -> locator.locate(new StubViewModelA()));
        }
    }

    @Nested
    @DisplayName("when the locator has one registration")
    class WhenOneRegistration {

        private ViewLocator<Region> registerA() {
            var locator = new ViewLocator<Region>();
            locator.register(StubViewModelA.class, vm -> new StubViewA());
            return locator;
        }

        @Test
        @DisplayName("the expected view is returned when locating the registered viewModel")
        void resolvesRegisteredViewModel() {
            var locator = registerA();

            Region view = locator.locate(new StubViewModelA());

            assertInstanceOf(StubViewA.class, view);
        }

        @Test
        @DisplayName("an exception is thrown when locating an unregistered viewModel")
        void throwsForUnregisteredViewModel() {
            var locator = registerA();

            assertThrows(IllegalStateException.class, () -> locator.locate(new StubViewModelB()));
        }
    }

    @Nested
    @DisplayName("when the locator has multiple registrations")
    class WhenMultipleRegistrations {

        private ViewLocator<Region> registerAAndB() {
            var locator = new ViewLocator<Region>();
            locator.register(StubViewModelA.class, vm -> new StubViewA());
            locator.register(StubViewModelB.class, vm -> new StubViewB());
            return locator;
        }

        @ParameterizedTest(name = "resolves {0}")
        @MethodSource("mvvm.example.core.view.ViewLocatorTest#registeredViewModels")
        @DisplayName("the expected view is returned when locating a registered viewModel")
        void resolvesRegisteredViewModel(String label, Supplier<Object> viewModel, Class<? extends Region> expectedView) {
            var locator = registerAAndB();

            assertInstanceOf(expectedView, locator.locate(viewModel.get()));
        }

        @Test
        @DisplayName("an exception is thrown when locating an unregistered viewModel")
        void throwsForUnregisteredViewModel() {
            var locator = registerAAndB();

            assertThrows(IllegalStateException.class, () -> locator.locate(new StubViewModelC()));
        }
    }

    private static Stream<Arguments> registeredViewModels() {
        return Stream.of(
            Arguments.of("StubViewModelA", (Supplier<Object>) StubViewModelA::new, StubViewA.class),
            Arguments.of("StubViewModelB", (Supplier<Object>) StubViewModelB::new, StubViewB.class)
        );
    }
}
