package com.client_app.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.PaymentMethodCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CustomerService {

    public Customer createCustomer(String email) {
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        Customer customer = null;
        try {
            customer = Customer.create(params);
        } catch (StripeException e) {
            log.error(String.format("Stripe exception: %s", e.getMessage()));
        }

        return customer;
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
        //
        return paymentMethod;
    }

    public PaymentMethod attachPaymentMethodToCustomer(String paymentMethodId, String customerId) throws Exception {
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

        PaymentMethodAttachParams attachParams =
                PaymentMethodAttachParams.builder()
                        .setCustomer(customerId)
                        .build();

        paymentMethod.attach(attachParams);

        CustomerUpdateParams updateParams =
                CustomerUpdateParams.builder()
                        .setInvoiceSettings(
                                CustomerUpdateParams.InvoiceSettings.builder()
                                        .setDefaultPaymentMethod(paymentMethodId)
                                        .build()
                        )
                        .build();
        Customer customer = Customer.retrieve(customerId);
        customer.update(updateParams);

        return paymentMethod;
    }

    public String retrievePaymentMethods(String customerId) {
        String paymentMethidId = null;
        try {
            Customer customer = Customer.retrieve(customerId);

            String defaultPaymentMethodId = customer.getInvoiceSettings().getDefaultPaymentMethod();

            if (defaultPaymentMethodId != null && !defaultPaymentMethodId.isEmpty()) {
                PaymentMethod defaultPaymentMethod = PaymentMethod.retrieve(defaultPaymentMethodId);
                paymentMethidId = defaultPaymentMethod.getId();
                log.info("+++++++++++++++++++++++++++++++++++++++++++++ " + defaultPaymentMethodId);
                if (defaultPaymentMethod.getCard() != null) {
                    System.out.println("Marka karty: " + defaultPaymentMethod.getCard().getBrand());
                    System.out.println("Ostatnie 4 cyfry: " + defaultPaymentMethod.getCard().getLast4());
                }
            } else {
                System.out.println("Klient nie posiada ustawionej domyślnej metody płatności.");
            }
        } catch (StripeException e) {
            System.err.println("Błąd podczas pobierania danych: " + e.getMessage());
        }

        return paymentMethidId;
    }
}
