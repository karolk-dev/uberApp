package com.server_app.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    public void setUp() {
        paymentService = new PaymentService();
    }

    @Test
    void testCreatePaymentIntent_success() throws StripeException {
        // Given
        String customerId = "cus_123";
        Long amount = 1000L;
        String currency = "usd";
        String paymentMethodId = "pm_123";
        PaymentIntent mockedPaymentIntent = Mockito.mock(PaymentIntent.class);

        // When & Then
        try (MockedStatic<PaymentIntent> paymentIntentStaticMock = Mockito.mockStatic(PaymentIntent.class)) {
            paymentIntentStaticMock.when(() -> PaymentIntent.create(Mockito.anyMap()))
                    .thenReturn(mockedPaymentIntent);

            PaymentIntent result = paymentService.createPaymentIntent(customerId, amount, currency, paymentMethodId);
            assertEquals(mockedPaymentIntent, result);
        }
    }

    @Test
    void testCreatePaymentIntent_invalidPaymentMethodId_null() {
        String customerId = "cus_123";
        Long amount = 1000L;
        String currency = "usd";
        String paymentMethodId = null;

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.createPaymentIntent(customerId, amount, currency, paymentMethodId));
        assertEquals("paymentMethodId must not be empty", exception.getMessage());
    }

    @Test
    void testCreatePaymentIntent_invalidPaymentMethodId_blank() {
        String customerId = "cus_123";
        Long amount = 1000L;
        String currency = "usd";
        String paymentMethodId = "   ";

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.createPaymentIntent(customerId, amount, currency, paymentMethodId));
        assertEquals("paymentMethodId must not be empty", exception.getMessage());
    }

    @Test
    void testUpdatePaymentIntent_success() throws StripeException {
        String paymentIntentId = "pi_123";
        Long finalAmount = 1500L;
        PaymentIntent mockedPaymentIntent = Mockito.mock(PaymentIntent.class);
        PaymentIntent updatedPaymentIntent = Mockito.mock(PaymentIntent.class);

        try (MockedStatic<PaymentIntent> paymentIntentStaticMock = Mockito.mockStatic(PaymentIntent.class)) {
            paymentIntentStaticMock.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenReturn(mockedPaymentIntent);
            Mockito.when(mockedPaymentIntent.update(Mockito.anyMap()))
                    .thenReturn(updatedPaymentIntent);

            PaymentIntent result = paymentService.updatePaymentIntent(paymentIntentId, finalAmount);
            assertEquals(updatedPaymentIntent, result);
        }
    }

    @Test
    void testUpdatePaymentIntent_withConfirmation_success() throws StripeException {
        String paymentIntentId = "pi_456";
        long newFinalPrice = 2000L;
        PaymentIntent mockedPaymentIntent = Mockito.mock(PaymentIntent.class);
        PaymentIntent updatedPaymentIntent = Mockito.mock(PaymentIntent.class);
        PaymentIntent confirmedPaymentIntent = Mockito.mock(PaymentIntent.class);

        try (MockedStatic<PaymentIntent> paymentIntentStaticMock = Mockito.mockStatic(PaymentIntent.class)) {
            paymentIntentStaticMock.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenReturn(mockedPaymentIntent, mockedPaymentIntent, confirmedPaymentIntent);
            Mockito.when(mockedPaymentIntent.update(Mockito.anyMap()))
                    .thenReturn(updatedPaymentIntent);
            PaymentIntent result = paymentService.updatePaymentIntent(paymentIntentId, newFinalPrice);
            assertEquals(updatedPaymentIntent, result);
        }
    }

    @Test
    void testCreateCustomer_success() throws StripeException {
        String email = "test@example.com";
        Customer mockedCustomer = Mockito.mock(Customer.class);

        try (MockedStatic<Customer> customerStaticMock = Mockito.mockStatic(Customer.class)) {
            customerStaticMock.when(() -> Customer.create(Mockito.anyMap()))
                    .thenReturn(mockedCustomer);

            Customer result = paymentService.createCustomer(email);
            assertEquals(mockedCustomer, result);
        }
    }

//    @Test
//    void testCreatePaymentMethod_success() throws StripeException {
//        String token = "tok_123";
//        PaymentMethod mockedPaymentMethod = Mockito.mock(PaymentMethod.class);
//
//        try (MockedStatic<PaymentMethod> paymentMethodStaticMock = Mockito.mockStatic(PaymentMethod.class)) {
//            paymentMethodStaticMock.when(() -> PaymentMethod.create((Map<String, Object>) Mockito.any()))
//                    .thenReturn(mockedPaymentMethod);
//
//            PaymentMethod result = paymentService.createPaymentMethod(token);
//            assertEquals(mockedPaymentMethod, result);
//        }
//    }

    @Test
    void testConfirmPaymentIntent_success() throws StripeException {
        String paymentIntentId = "pi_confirm";
        PaymentIntent mockedPaymentIntent = Mockito.mock(PaymentIntent.class);
        PaymentIntent confirmedPaymentIntent = Mockito.mock(PaymentIntent.class);

        try (MockedStatic<PaymentIntent> paymentIntentStaticMock = Mockito.mockStatic(PaymentIntent.class)) {
            paymentIntentStaticMock.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenReturn(mockedPaymentIntent);
            Mockito.when(mockedPaymentIntent.confirm())
                    .thenReturn(confirmedPaymentIntent);

            PaymentIntent result = paymentService.confirmPaymentIntent(paymentIntentId);
            assertEquals(confirmedPaymentIntent, result);
        }
    }
}
