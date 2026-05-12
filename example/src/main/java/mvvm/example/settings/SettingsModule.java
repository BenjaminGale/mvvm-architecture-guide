package mvvm.example.settings;

import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.ViewRouter;

public class SettingsModule {
    private final ViewRouter viewRouter;
    private final Runnable onBack;

    public SettingsModule(ViewLocator viewLocator, ViewRouter viewRouter, Runnable onBack) {
        this.viewRouter = viewRouter;
        this.onBack = onBack;

        viewLocator.register(SettingsViewModel.class, SettingsView::new);
    }

    public void routeToOrders() {
        viewRouter.route(new SettingsViewModel(onBack));
    }
}
