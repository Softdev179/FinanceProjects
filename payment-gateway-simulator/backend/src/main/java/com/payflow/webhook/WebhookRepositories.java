package com.payflow.webhook;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint,String>{List<WebhookEndpoint> findByMerchantIdAndActiveTrue(String merchantId);}
interface WebhookEventRepository extends JpaRepository<WebhookEvent,String>{List<WebhookEvent> findTop50ByOrderByCreatedAtDesc();}
