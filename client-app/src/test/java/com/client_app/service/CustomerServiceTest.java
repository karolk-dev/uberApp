package com.client_app.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.PaymentMethodCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    private final CustomerService customerService = new CustomerService();

    @Test
    public void testCreateCustomer_success() throws StripeException {
        String email = "test@example.com";
        Customer mockCustomer = mock(Customer.class);

        try (MockedStatic<Customer> customerStaticMock = Mockito.mockStatic(Customer.class)) {
            customerStaticMock.when(() -> Customer.create(anyMap()))
                    .thenReturn(mockCustomer);

            Customer result = customerService.createCustomer(email);
            assertNotNull(result);
            customerStaticMock.when(() -> Customer.create(ArgumentMatchers.<String, Object>anyMap()))
                    .thenReturn(mockCustomer);
        }
    }

    @Test
    public void testCreateCustomer_stripeException() throws StripeException {
        String email = "test@example.com";
        try (MockedStatic<Customer> customerStaticMock = Mockito.mockStatic(Customer.class)) {
            customerStaticMock.when(() -> Customer.create(anyMap()))
                    .thenThrow(new StripeException("Test exception", "", "", 400) {});

            Customer result = customerService.createCustomer(email);
            assertNull(result);
            customerStaticMock.verify(() -> Customer.create(anyMap()));
        }
    }

    @Test
    public void testCreatePaymentMethod_success() throws StripeException {
        String token = "tok_test";
        PaymentMethod mockPaymentMethod = mock(PaymentMethod.class);
        when(mockPaymentMethod.getId()).thenReturn("pm_123");

        try (MockedStatic<PaymentMethod> paymentMethodStaticMock = Mockito.mockStatic(PaymentMethod.class)) {
            paymentMethodStaticMock.when(() -> PaymentMethod.create(any(PaymentMethodCreateParams.class)))
                    .thenReturn(mockPaymentMethod);

            PaymentMethod result = customerService.createPaymentMethod(token);
            assertNotNull(result);
            assertEquals("pm_123", result.getId());
            paymentMethodStaticMock.verify(() -> PaymentMethod.create(any(PaymentMethodCreateParams.class)));
        }
    }

    @Test
    public void testCreatePaymentMethod_throwsStripeException() throws StripeException {
        String token = "tok_test";
        try (MockedStatic<PaymentMethod> paymentMethodStaticMock = Mockito.mockStatic(PaymentMethod.class)) {
            paymentMethodStaticMock.when(() -> PaymentMethod.create(any(PaymentMethodCreateParams.class)))
                    .thenThrow(new StripeException("Error creating PaymentMethod", "", "", 500) {});

            assertThrows(StripeException.class, () -> customerService.createPaymentMethod(token));
        }
    }

    @Test
    public void testAttachPaymentMethodToCustomer_success() throws Exception {
        String paymentMethodId = "pm_123";
        String customerId = "cus_456";

        PaymentMethod mockPaymentMethod = mock(PaymentMethod.class);
        // Ustawienie identyfikatora na mocku głównym
        when(mockPaymentMethod.getId()).thenReturn(paymentMethodId);

        Customer mockCustomer = mock(Customer.class);
        when(mockCustomer.update(any(CustomerUpdateParams.class))).thenReturn(mockCustomer);

        try (MockedStatic<PaymentMethod> paymentMethodStaticMock = Mockito.mockStatic(PaymentMethod.class);
             MockedStatic<Customer> customerStaticMock = Mockito.mockStatic(Customer.class)) {

            // Mockowanie statycznej metody PaymentMethod.retrieve
            paymentMethodStaticMock.when(() -> PaymentMethod.retrieve(eq(paymentMethodId)))
                    .thenReturn(mockPaymentMethod);
            // Mockowanie statycznej metody Customer.retrieve
            customerStaticMock.when(() -> Customer.retrieve(eq(customerId)))
                    .thenReturn(mockCustomer);

            // Ustal, że wywołanie attach zwraca ten sam mockPaymentMethod
            when(mockPaymentMethod.attach(any(PaymentMethodAttachParams.class)))
                    .thenReturn(mockPaymentMethod);

            PaymentMethod result = customerService.attachPaymentMethodToCustomer(paymentMethodId, customerId);
            assertNotNull(result);
            assertEquals(paymentMethodId, result.getId());

            paymentMethodStaticMock.verify(() -> PaymentMethod.retrieve(eq(paymentMethodId)));
            verify(mockPaymentMethod).attach(any(PaymentMethodAttachParams.class));
            customerStaticMock.verify(() -> Customer.retrieve(eq(customerId)));
            verify(mockCustomer).update(any(CustomerUpdateParams.class));
        }
    }


    @Test
    public void testRetrievePaymentMethods_success() throws StripeException {
        String customerId = "cus_456";
        String defaultPaymentMethodId = "pm_789";

        Customer mockCustomer = mock(Customer.class);
        Customer.InvoiceSettings mockInvoiceSettings = mock(Customer.InvoiceSettings.class);
        when(mockInvoiceSettings.getDefaultPaymentMethod()).thenReturn(defaultPaymentMethodId);
        when(mockCustomer.getInvoiceSettings()).thenReturn(mockInvoiceSettings);

        PaymentMethod mockPaymentMethod = mock(PaymentMethod.class);
        when(mockPaymentMethod.getId()).thenReturn(defaultPaymentMethodId);
        PaymentMethod.Card mockCard = mock(PaymentMethod.Card.class);
        when(mockCard.getBrand()).thenReturn("Visa");
        when(mockCard.getLast4()).thenReturn("4242");
        when(mockPaymentMethod.getCard()).thenReturn(mockCard);

        try (MockedStatic<Customer> customerStaticMock = Mockito.mockStatic(Customer.class);
             MockedStatic<PaymentMethod> paymentMethodStaticMock = Mockito.mockStatic(PaymentMethod.class)) {

            customerStaticMock.when(() -> Customer.retrieve(eq(customerId)))
                    .thenReturn(mockCustomer);

            paymentMethodStaticMock.when(() -> PaymentMethod.retrieve(eq(defaultPaymentMethodId)))
                    .thenReturn(mockPaymentMethod);

            String result = customerService.retrievePaymentMethods(customerId);
            assertNotNull(result);
            assertEquals(defaultPaymentMethodId, result);

            customerStaticMock.verify(() -> Customer.retrieve(eq(customerId)));
            paymentMethodStaticMock.verify(() -> PaymentMethod.retrieve(eq(defaultPaymentMethodId)));
        }
    }

    @Test
    public void testRetrievePaymentMethods_noDefault() throws StripeException {
        String customerId = "cus_no_default";

        Customer mockCustomer = mock(Customer.class);
        Customer.InvoiceSettings mockInvoiceSettings = mock(Customer.InvoiceSettings.class);
        when(mockInvoiceSettings.getDefaultPaymentMethod()).thenReturn("");
        when(mockCustomer.getInvoiceSettings()).thenReturn(mockInvoiceSettings);

        try (MockedStatic<Customer> customerStaticMock = Mockito.mockStatic(Customer.class)) {
            customerStaticMock.when(() -> Customer.retrieve(eq(customerId)))
                    .thenReturn(mockCustomer);

            String result = customerService.retrievePaymentMethods(customerId);
            assertNull(result);
            customerStaticMock.verify(() -> Customer.retrieve(eq(customerId)));
        }
    }

    @Test
    public void testRetrievePaymentMethods_stripeException() throws StripeException {
        String customerId = "cus_exception";
        try (MockedStatic<Customer> customerStaticMock = Mockito.mockStatic(Customer.class)) {
            customerStaticMock.when(() -> Customer.retrieve(eq(customerId)))
                    .thenThrow(new StripeException("Error", "", "", 500) {});
            String result = customerService.retrievePaymentMethods(customerId);
            assertNull(result);
            customerStaticMock.verify(() -> Customer.retrieve(eq(customerId)));
        }
    }
}
