package mvvm.example.core.view;

import javafx.scene.layout.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ViewRouter {

    private final ViewFactory viewFactory;
    private final Map<Class<?>, Consumer<Region>> listeners = new HashMap<>();

    public ViewRouter(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    public <V extends Region> void addListener(Class<V> viewClass, Consumer<V> listener) {
        listeners.put(viewClass, view -> listener.accept(viewClass.cast(view)));
    }

    public void navigateTo(Object viewModel) {
        var view = viewFactory.create(viewModel);
        var listener = listeners.get(view.getClass());
        if (listener != null) listener.accept(view);
    }
}
