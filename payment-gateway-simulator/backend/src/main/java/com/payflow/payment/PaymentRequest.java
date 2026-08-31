package com.payflow.payment;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record PaymentRequest(@NotBlank String merchantId, @NotNull @DecimalMin("1.00") BigDecimal amount, @NotBlank @Size(min=3,max=3) String currency, @NotBlank String method, String description, @Email String customerEmail) {}
