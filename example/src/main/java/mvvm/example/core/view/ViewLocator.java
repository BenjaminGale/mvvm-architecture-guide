package mvvm.example.core.view;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ViewLocator<TView> {

    private final Map<Class<?>, Function<Object, TView>> registry = new HashMap<>();

    public <TViewModel> void register(Class<TViewModel> vmClass, Function<TViewModel, TView> viewFactory) {
        registry.put(vmClass, vm -> viewFactory.apply(vmClass.cast(vm)));
    }

    public TView locate(Object viewModel) {
        var factory = registry.get(viewModel.getClass());

        if (factory == null)
            throw new IllegalStateException("No view registered for " + viewModel.getClass().getSimpleName());

        return factory.apply(viewModel);
    }
}
