package com.mibanco.api.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepositRequest {

    @NotBlank(message = "El número de cuenta es obligatorio")
    private String accountNumber;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El depósito debe ser superior a cero")
    @Digits(integer = 13, fraction = 2, message = "El monto admite hasta 13 enteros y 2 decimales")
    private BigDecimal amount;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String description;
}
