package com.driver_app.model;

import com.uber.common.Coordinates;
import com.uber.common.model.DriverStatus;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Embedded;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String uuid;

    @NotBlank
    private String name;

    @NotBlank
    private String nip;

    private String companyName;

    private String companyStatus;

    @OneToOne
    @JoinColumn(name = "carId", referencedColumnName = "id")
    private Car car;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "location_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "location_longitude"))
    })
    @NotNull
    private Coordinates coordinates;

    private boolean isAvailable = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DriverStatus status = DriverStatus.OFFLINE;
}