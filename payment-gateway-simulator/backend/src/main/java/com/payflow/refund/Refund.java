package com.payflow.refund;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="refunds")
public class Refund {
 @Id private String id; @Column(nullable=false) private String paymentId; @Column(nullable=false,precision=19,scale=2) private BigDecimal amount; @Column(nullable=false) private String reason; @Column(nullable=false) private Instant createdAt;
 protected Refund(){} public Refund(String paymentId, BigDecimal amount,String reason){this.id="rfnd_"+UUID.randomUUID().toString().replace("-","").substring(0,20);this.paymentId=paymentId;this.amount=amount;this.reason=reason;this.createdAt=Instant.now();}
 public String getId(){return id;} public String getPaymentId(){return paymentId;} public BigDecimal getAmount(){return amount;} public String getReason(){return reason;} public Instant getCreatedAt(){return createdAt;}
}
