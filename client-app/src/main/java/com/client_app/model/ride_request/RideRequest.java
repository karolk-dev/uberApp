package com.client_app.model.ride_request;

import com.uber.common.Coordinates;
import com.uber.common.productSelector.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ride_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String clientUuid;

    @Column(nullable = false)
    private Coordinates pickupLocation;

    @Column(nullable = false)
    private Coordinates destinationLocation;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Product product;

    private String customerId;

    private String paymentMethodId;
}