package com.server_app.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.PaymentMethodCreateParams;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class PaymentService {

    public PaymentIntent createPaymentIntent(String customerId, Long amount, String currency, String paymentMethotId) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        if (paymentMethotId == null || paymentMethotId.isBlank()) {
            throw new IllegalArgumentException("paymentMethodId must not be empty");
        }

        params.put("amount", amount);
        params.put("currency", currency);
        params.put("customer", customerId);
        params.put("payment_method", paymentMethotId);
        params.put("automatic_payment_methods", Map.of(
                "enabled", true,
                "allow_redirects", "never"
        ));
        params.put("capture_method", "manual");
        params.put("confirm", false);

        return PaymentIntent.create(params);
    }

    public PaymentIntent updatePaymentIntent(String paymentIntentId, Long finalAmount) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        Map<String, Object> params = new HashMap<>();
        params.put("amount", finalAmount);
        return paymentIntent.update(params);
    }

    public PaymentIntent capturePayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return paymentIntent.capture();
        } catch (StripeException e) {
            return new PaymentIntent();
        }
    }

    public Customer createCustomer(String email) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        return Customer.create(params);
    }

    public PaymentMethod createPaymentMethod(String token) throws StripeException {
        PaymentMethodCreateParams params = PaymentMethodCreateParams.builder()
                .setType(PaymentMethodCreateParams.Type.CARD)
                .setCard(
                        PaymentMethodCreateParams.Token.builder()
                                .setToken(token)
                                .build()
                )
                .build();

        PaymentMethod paymentMethod = PaymentMethod.create(params);
        System.out.println("Utworzono PaymentMethod: " + paymentMethod.getId());
        return paymentMethod;
    }

    public PaymentMethod attachPaymentMethodToCustomer(String paymentMethodId, String customerId) throws Exception {
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

        PaymentMethodAttachParams attachParams =
                PaymentMethodAttachParams.builder()
                        .setCustomer(customerId)
                        .build();

        return paymentMethod.attach(attachParams);
    }

    public PaymentIntent updatePaymentIntent(String paymentIntentId, long newFinalPrice) throws StripeException {

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        Map<String, Object> params = new HashMap<>();
        params.put("amount", newFinalPrice);

        PaymentIntent updatedPaymentIntent = paymentIntent.update(params);
        confirmPaymentIntent(paymentIntentId);

        return updatedPaymentIntent;
    }

    public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.confirm();
    }
}
