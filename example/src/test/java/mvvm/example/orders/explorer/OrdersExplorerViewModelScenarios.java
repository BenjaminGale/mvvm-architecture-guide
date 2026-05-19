package mvvm.example.orders.explorer;

import mvvm.example.orders.MockOrders;
import mvvm.example.orders.domain.OrderSummary;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public class OrdersExplorerViewModelScenarios {

    private static final LocalDate RECENT = LocalDate.of(2026, 6, 10);
    private static final LocalDate OLDER = LocalDate.of(2026, 6, 1);
    private static final LocalDate OVERDUE = LocalDate.of(2026, 4, 1);

    public static Stream<Arguments> statusMessageCases() {
        return Stream.of(
            Arguments.of(
                "no orders",
                List.of(),
                0, 0
            ),
            Arguments.of(
                "single non-isOverdue order",
                List.of(MockOrders.summaryOf("1", RECENT)),
                1, 0
            ),
            Arguments.of(
                "mixed isOverdue and non-isOverdue orders",
                List.of(
                    MockOrders.summaryOf("1", RECENT),
                    MockOrders.summaryOf("2", OVERDUE)
                ),
                2, 1
            ),
            Arguments.of(
                "all orders isOverdue",
                List.of(
                    MockOrders.summaryOf("1", OVERDUE),
                    MockOrders.summaryOf("2", OVERDUE),
                    MockOrders.summaryOf("3", OVERDUE)
                ),
                3, 3
            )
        );
    }

    static Stream<Arguments> sortingCases() {
        return Stream.of(
            Arguments.of(
                "reverse chronological input",
                List.of(
                    MockOrders.summaryOf("older", OLDER),
                    MockOrders.summaryOf("recent", RECENT)
                ),
                List.of("recent", "older")
            ),
            Arguments.of(
                "already sorted input",
                List.of(
                    MockOrders.summaryOf("recent", RECENT),
                    MockOrders.summaryOf("older", OLDER)
                ),
                List.of("recent", "older")
            ),
            Arguments.of(
                "same-date orders preserve insertion order",
                List.of(
                    MockOrders.summaryOf("A", RECENT),
                    MockOrders.summaryOf("B", RECENT),
                    MockOrders.summaryOf("C", RECENT)
                ),
                List.of("A", "B", "C")
            )
        );
    }

    static Stream<Arguments> refreshListCases() {
        return Stream.of(
            Arguments.of(
                "no orders",
                List.of(),
                List.of()
            ),
            Arguments.of(
                "single order",
                List.of(MockOrders.summaryOf("1", RECENT)),
                List.of("1")
            ),
            Arguments.of(
                "multiple orders",
                List.of(
                    MockOrders.summaryOf("1", RECENT),
                    MockOrders.summaryOf("2", RECENT),
                    MockOrders.summaryOf("3", RECENT)
                ),
                List.of("1", "2", "3")
            )
        );
    }
}
