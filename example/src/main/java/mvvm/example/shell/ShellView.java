package mvvm.example.shell;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import mvvm.example.core.view.ViewLocator;

public class ShellView extends BorderPane {

    public ShellView(ShellViewModel viewModel, ViewLocator<Region> viewLocator) {
        var content = new BorderPane();
        content.setTop(new ToolbarView(viewModel));
        content.setCenter(new TabAreaView(viewModel, viewLocator));

        setLeft(new WorkspaceSidebarView(viewModel));
        setCenter(content);
    }
}
