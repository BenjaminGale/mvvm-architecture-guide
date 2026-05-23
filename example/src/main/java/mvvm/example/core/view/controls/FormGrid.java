package mvvm.example.core.view.controls;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class FormGrid extends GridPane {

    private int nextRow = 0;

    public FormGrid() {
        this(new Insets(16));
    }

    public FormGrid(Insets padding) {
        setHgap(8);
        setVgap(8);
        setPadding(padding);
        var labelCol = new ColumnConstraints();
        var fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        getColumnConstraints().addAll(labelCol, fieldCol);
    }

    public void addRow(String label, Node field) {
        add(new Label(label), 0, nextRow);
        add(field, 1, nextRow);
        nextRow++;
    }
}
