package com.payflow.webhook;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="webhook_events")
public class WebhookEvent {
 @Id private String id; @Column(nullable=false) private String merchantId; @Column(nullable=false) private String type; @Column(nullable=false,columnDefinition="TEXT") private String payload; @Column(nullable=false) private String deliveryStatus; @Column(nullable=false) private int attempts; private String response; @Column(nullable=false) private Instant createdAt=Instant.now();
 protected WebhookEvent(){} public WebhookEvent(String merchantId,String type,String payload){id="evt_"+UUID.randomUUID().toString().replace("-","").substring(0,20);this.merchantId=merchantId;this.type=type;this.payload=payload;this.deliveryStatus="PENDING";}
 public String getId(){return id;} public String getMerchantId(){return merchantId;} public String getType(){return type;} public String getPayload(){return payload;} public String getDeliveryStatus(){return deliveryStatus;} public int getAttempts(){return attempts;} public String getResponse(){return response;} public Instant getCreatedAt(){return createdAt;}
 public void delivered(String response){attempts++;deliveryStatus="DELIVERED";this.response=response;} public void failed(String response){attempts++;deliveryStatus="FAILED";this.response=response;}
}
