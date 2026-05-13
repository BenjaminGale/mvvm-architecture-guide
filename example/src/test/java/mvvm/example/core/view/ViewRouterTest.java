package mvvm.example.core.view;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ViewRouter")
class ViewRouterTest {

    private static class StubViewModel {}
    private static class StubView extends Pane {}
    private static class OtherView extends Pane {}

    private static class StubListener<V extends Region> {
        private V received;

        void accept(V view) { received = view; }

        void assertReceivedView(V expected) { assertSame(expected, received); }
        void assertNoViewReceived() { assertNull(received); }
    }

    private ViewRouter routerWith(Class<?> vmClass, Region view) {
        var locator = new ViewLocator();
        locator.register(vmClass, _ -> view);

        return new ViewRouter(locator);
    }

    private <V extends Region> ViewRouter routerWith(Class<?> vmClass, Class<V> viewClass, V view, StubListener<V> listener) {
        var router = routerWith(vmClass, view);
        router.addListener(viewClass, listener::accept);
        return router;
    }

    @Nested
    @DisplayName("when routing to a ViewModel with a registered listener")
    class WhenListenerRegistered {

        @Test
        @DisplayName("the view is delivered to the listener")
        void viewDeliveredToListener() {
            var view = new StubView();
            var listener = new StubListener<StubView>();
            var router = routerWith(StubViewModel.class, StubView.class, view, listener);

            router.route(new StubViewModel());

            listener.assertReceivedView(view);
        }
    }

    @Nested
    @DisplayName("when no listener is registered for the resolved view")
    class WhenNoListenerRegistered {

        @Test
        @DisplayName("no error is thrown")
        void noErrorThrown() {
            var router = routerWith(StubViewModel.class, new StubView());

            assertDoesNotThrow(() -> router.route(new StubViewModel()));
        }

        @Test
        @DisplayName("listeners for other view types are not notified")
        void listenersForOtherViewTypesNotNotified() {
            var otherListener = new StubListener<OtherView>();
            var router = routerWith(StubViewModel.class, StubView.class, new StubView(), new StubListener<>());
            router.addListener(OtherView.class, otherListener::accept);

            router.route(new StubViewModel());

            otherListener.assertNoViewReceived();
        }
    }
}
