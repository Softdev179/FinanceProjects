package com.payflow.webhook;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="webhook_endpoints")
public class WebhookEndpoint {
 @Id private String id; @Column(nullable=false) private String merchantId; @Column(nullable=false) private String url; @Column(nullable=false) private boolean active=true; @Column(nullable=false) private Instant createdAt=Instant.now();
 protected WebhookEndpoint(){} public WebhookEndpoint(String merchantId,String url){id="wh_"+UUID.randomUUID().toString().replace("-","").substring(0,16);this.merchantId=merchantId;this.url=url;}
 public String getId(){return id;} public String getMerchantId(){return merchantId;} public String getUrl(){return url;} public boolean isActive(){return active;} public Instant getCreatedAt(){return createdAt;}
}
