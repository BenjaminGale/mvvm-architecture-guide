package mvvm.example.orders.explorer;

import mvvm.example.orders.MockOrders;
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
                "single non-overdue order",
                List.of(MockOrders.of("1", RECENT)),
                1, 0
            ),
            Arguments.of(
                "mixed overdue and non-overdue orders",
                List.of(
                    MockOrders.of("1", RECENT),
                    MockOrders.of("2", OVERDUE)
                ),
                2, 1
            ),
            Arguments.of(
                "all orders overdue",
                List.of(
                    MockOrders.of("1", OVERDUE),
                    MockOrders.of("2", OVERDUE),
                    MockOrders.of("3", OVERDUE)
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
                    MockOrders.of("older", OLDER),
                    MockOrders.of("recent", RECENT)
                ),
                List.of("recent", "older")
            ),
            Arguments.of(
                "already sorted input",
                List.of(
                    MockOrders.of("recent", RECENT),
                    MockOrders.of("older", OLDER)
                ),
                List.of("recent", "older")
            ),
            Arguments.of(
                "same-date orders preserve insertion order",
                List.of(
                    MockOrders.of("A", RECENT),
                    MockOrders.of("B", RECENT),
                    MockOrders.of("C", RECENT)
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
                List.of(MockOrders.of("1", RECENT)),
                List.of("1")
            ),
            Arguments.of(
                "multiple orders",
                List.of(
                    MockOrders.of("1", RECENT),
                    MockOrders.of("2", RECENT),
                    MockOrders.of("3", RECENT)
                ),
                List.of("1", "2", "3")
            )
        );
    }
}
