package com.payflow.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="payments", uniqueConstraints=@UniqueConstraint(name="uk_payment_idempotency", columnNames="idempotency_key"))
public class Payment {
  @Id private String id;
  @Column(name="idempotency_key", nullable=false, updatable=false) private String idempotencyKey;
  @Column(nullable=false) private String merchantId;
  @Column(nullable=false, precision=19, scale=2) private BigDecimal amount;
  @Column(nullable=false, precision=19, scale=2) private BigDecimal refundedAmount = BigDecimal.ZERO;
  @Column(nullable=false, length=3) private String currency;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private PaymentStatus status;
  @Column(nullable=false) private String method;
  private String description; private String customerEmail; private String failureReason;
  @Column(nullable=false) private int attemptCount;
  @Column(nullable=false) private Instant createdAt; private Instant authorizedAt; private Instant capturedAt; private Instant updatedAt;

  protected Payment() {}
  public Payment(String key, PaymentRequest r) {
    id="pay_"+UUID.randomUUID().toString().replace("-","").substring(0,20); idempotencyKey=key; merchantId=r.merchantId();
    amount=r.amount(); currency=r.currency().toUpperCase(); method=r.method(); description=r.description(); customerEmail=r.customerEmail();
    status=PaymentStatus.CREATED; createdAt=updatedAt=Instant.now();
  }
  public String getId(){return id;} public String getIdempotencyKey(){return idempotencyKey;} public String getMerchantId(){return merchantId;}
  public BigDecimal getAmount(){return amount;} public BigDecimal getRefundedAmount(){return refundedAmount;} public String getCurrency(){return currency;}
  public PaymentStatus getStatus(){return status;} public String getMethod(){return method;} public String getDescription(){return description;}
  public String getCustomerEmail(){return customerEmail;} public String getFailureReason(){return failureReason;} public int getAttemptCount(){return attemptCount;}
  public Instant getCreatedAt(){return createdAt;} public Instant getAuthorizedAt(){return authorizedAt;} public Instant getCapturedAt(){return capturedAt;} public Instant getUpdatedAt(){return updatedAt;}
  public void authorize(boolean success, String reason){attemptCount++; updatedAt=Instant.now(); if(success){status=PaymentStatus.AUTHORIZED;authorizedAt=updatedAt;failureReason=null;}else{status=PaymentStatus.FAILED;failureReason=reason;}}
  public void capture(){status=PaymentStatus.CAPTURED;capturedAt=updatedAt=Instant.now();}
  public void refund(BigDecimal value){refundedAmount=refundedAmount.add(value);status=refundedAmount.compareTo(amount)==0?PaymentStatus.REFUNDED:PaymentStatus.PARTIALLY_REFUNDED;updatedAt=Instant.now();}
}
