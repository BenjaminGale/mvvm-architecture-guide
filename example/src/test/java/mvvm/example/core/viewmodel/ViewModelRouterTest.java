package mvvm.example.core.viewmodel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ViewModelRouter")
class ViewModelRouterTest {

    private static class StubViewModel {}
    private static class OtherViewModel {}

    @Nested
    @DisplayName("when dispatching to a registered handler")
    class WhenHandlerRegistered {

        @Test
        @DisplayName("the handler receives the dispatched view model")
        void listenerReceivesViewModel() {
            var received = new AtomicReference<StubViewModel>();
            var router = new ViewModelRouter();
            var vm = new StubViewModel();
            router.receive(StubViewModel.class, received::set);

            router.dispatch(vm);

            assertSame(vm, received.get());
        }
    }

    @Nested
    @DisplayName("when no handler is registered for the view model type")
    class WhenNoHandlerRegistered {

        @Test
        @DisplayName("no error is thrown")
        void noErrorThrown() {
            var router = new ViewModelRouter();

            assertDoesNotThrow(() -> router.dispatch(new StubViewModel()));
        }

        @Test
        @DisplayName("handlers for other view model types are not notified")
        void otherListenersNotNotified() {
            var received = new AtomicReference<OtherViewModel>();
            var router = new ViewModelRouter();
            router.receive(OtherViewModel.class, received::set);

            router.dispatch(new StubViewModel());

            assertNull(received.get());
        }
    }
}
