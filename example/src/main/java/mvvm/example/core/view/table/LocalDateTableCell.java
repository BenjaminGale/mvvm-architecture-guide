package mvvm.example.core.view.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateTableCell<S> extends TableCell<S, LocalDate> {

    private final DateTimeFormatter formatter;

    public static <S> Callback<TableColumn<S, LocalDate>, TableCell<S, LocalDate>> forTableColumn(DateTimeFormatter formatter) {
        return c -> new LocalDateTableCell<>(formatter);
    }

    private LocalDateTableCell(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    protected void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);
        setText(empty || date == null ? null : formatter.format(date));
    }
}
