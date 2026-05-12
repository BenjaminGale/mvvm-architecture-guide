package mvvm.example.settings;

public class SettingsViewModel {

    private final Runnable onBack;

    public SettingsViewModel(Runnable onBack) {
        this.onBack = onBack;
    }

    public void back() {
        onBack.run();
    }
}
