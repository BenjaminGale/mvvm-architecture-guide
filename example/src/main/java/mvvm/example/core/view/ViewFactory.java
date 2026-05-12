package mvvm.example.core.view;

import javafx.scene.layout.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ViewFactory {

    private final Map<Class<?>, Function<Object, Region>> registry = new HashMap<>();

    public <VM> void register(Class<VM> vmClass, Function<VM, Region> factory) {
        registry.put(vmClass, vm -> factory.apply(vmClass.cast(vm)));
    }

    public Region create(Object viewModel) {
        var factory = registry.get(viewModel.getClass());

        if (factory == null) {
            throw new IllegalStateException("No view registered for " + viewModel.getClass().getSimpleName());
        }

        return factory.apply(viewModel);
    }
}
