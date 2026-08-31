package com.payflow.settlement;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="settlements")
public class Settlement {
 @Id private String id; @Column(nullable=false) private String merchantId; @Column(nullable=false,precision=19,scale=2) private BigDecimal grossAmount; @Column(nullable=false,precision=19,scale=2) private BigDecimal feeAmount; @Column(nullable=false,precision=19,scale=2) private BigDecimal netAmount; @Column(nullable=false) private String status; @Column(nullable=false) private Instant createdAt;
 protected Settlement(){} public Settlement(String merchantId,BigDecimal gross){id="setl_"+UUID.randomUUID().toString().replace("-","").substring(0,18);this.merchantId=merchantId;grossAmount=gross;feeAmount=gross.multiply(new BigDecimal("0.02")).setScale(2,java.math.RoundingMode.HALF_UP);netAmount=gross.subtract(feeAmount);status="PROCESSED";createdAt=Instant.now();}
 public String getId(){return id;} public String getMerchantId(){return merchantId;} public BigDecimal getGrossAmount(){return grossAmount;} public BigDecimal getFeeAmount(){return feeAmount;} public BigDecimal getNetAmount(){return netAmount;} public String getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
}
