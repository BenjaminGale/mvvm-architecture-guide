package mvvm.example.core.view.controls;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyTableCell<S> extends TableCell<S, BigDecimal> {

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.UK);

    public static <S> Callback<TableColumn<S, BigDecimal>, TableCell<S, BigDecimal>> forTableColumn() {
        return c -> new CurrencyTableCell<>();
    }

    @Override
    protected void updateItem(BigDecimal value, boolean empty) {
        super.updateItem(value, empty);
        setText(empty || value == null ? null : CURRENCY.format(value));
    }
}
