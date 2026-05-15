package mvvm.example.settings;

import mvvm.example.AppContext;

public class SettingsModule {

    public SettingsModule(AppContext appContext) {
        appContext.viewLocator().register(SettingsViewModel.class, SettingsView::new);
    }

    public SettingsViewModel settings(Runnable onBack) {
        return new SettingsViewModel(onBack);
    }
}
