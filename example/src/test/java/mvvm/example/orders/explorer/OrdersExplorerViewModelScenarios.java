package mvvm.example.orders.explorer;

import mvvm.example.orders.MockOrders;
import mvvm.example.orders.domain.queries.OrderSummary;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class OrdersExplorerViewModelScenarios {

    private static final LocalDate RECENT = LocalDate.of(2026, 6, 10);
    private static final LocalDate OLDER  = LocalDate.of(2026, 6, 1);
    private static final LocalDate OVERDUE = LocalDate.of(2026, 4, 1);

    private static final UUID ID_1      = UUID.randomUUID();
    private static final UUID ID_2      = UUID.randomUUID();
    private static final UUID ID_3      = UUID.randomUUID();
    private static final UUID ID_RECENT = UUID.randomUUID();
    private static final UUID ID_OLDER  = UUID.randomUUID();
    private static final UUID ID_A      = UUID.randomUUID();
    private static final UUID ID_B      = UUID.randomUUID();
    private static final UUID ID_C      = UUID.randomUUID();

    public static Stream<Arguments> statusMessageCases() {
        return Stream.of(
            Arguments.of(
                "no orders",
                List.of(),
                0, 0
            ),
            Arguments.of(
                "single non-isOverdue order",
                List.of(MockOrders.summaryOf(ID_1, RECENT)),
                1, 0
            ),
            Arguments.of(
                "mixed isOverdue and non-isOverdue orders",
                List.of(
                    MockOrders.summaryOf(ID_1, RECENT),
                    MockOrders.summaryOf(ID_2, OVERDUE)
                ),
                2, 1
            ),
            Arguments.of(
                "all orders isOverdue",
                List.of(
                    MockOrders.summaryOf(ID_1, OVERDUE),
                    MockOrders.summaryOf(ID_2, OVERDUE),
                    MockOrders.summaryOf(ID_3, OVERDUE)
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
                    MockOrders.summaryOf(ID_OLDER, OLDER),
                    MockOrders.summaryOf(ID_RECENT, RECENT)
                ),
                List.of(ID_RECENT, ID_OLDER)
            ),
            Arguments.of(
                "already sorted input",
                List.of(
                    MockOrders.summaryOf(ID_RECENT, RECENT),
                    MockOrders.summaryOf(ID_OLDER, OLDER)
                ),
                List.of(ID_RECENT, ID_OLDER)
            ),
            Arguments.of(
                "same-date orders preserve insertion order",
                List.of(
                    MockOrders.summaryOf(ID_A, RECENT),
                    MockOrders.summaryOf(ID_B, RECENT),
                    MockOrders.summaryOf(ID_C, RECENT)
                ),
                List.of(ID_A, ID_B, ID_C)
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
                List.of(MockOrders.summaryOf(ID_1, RECENT)),
                List.of(ID_1)
            ),
            Arguments.of(
                "multiple orders",
                List.of(
                    MockOrders.summaryOf(ID_1, RECENT),
                    MockOrders.summaryOf(ID_2, RECENT),
                    MockOrders.summaryOf(ID_3, RECENT)
                ),
                List.of(ID_1, ID_2, ID_3)
            )
        );
    }
}
