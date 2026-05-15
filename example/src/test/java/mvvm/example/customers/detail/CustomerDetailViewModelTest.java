package mvvm.example.customers.detail;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerDetailViewModel")
class CustomerDetailViewModelTest {

    private static final Customer A_CUSTOMER = new Customer("1", "Acme Ltd", "acme@example.com", CustomerStatus.ACTIVE);

    @Nested
    @DisplayName("when navigating back")
    class WhenNavigatingBack {

        @Test
        @DisplayName("the back navigation callback is invoked")
        void backCallbackInvoked() {
            var backInvoked = new AtomicBoolean(false);
            var vm = new CustomerDetailViewModel(A_CUSTOMER, () -> backInvoked.set(true));

            vm.back();

            assertTrue(backInvoked.get());
        }
    }
}
