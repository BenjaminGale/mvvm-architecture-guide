package mvvm.example.settings;

import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.viewmodel.ViewModelRouter;

public class SettingsModule {
    private final ViewModelRouter viewModelRouter;
    private final Runnable onBack;

    public SettingsModule(ViewLocator viewLocator, ViewModelRouter viewModelRouter, Runnable onBack) {
        this.viewModelRouter = viewModelRouter;
        this.onBack = onBack;

        viewLocator.register(SettingsViewModel.class, SettingsView::new);
    }

    public void routeToOrders() {
        viewModelRouter.dispatch(new SettingsViewModel(onBack));
    }
}
