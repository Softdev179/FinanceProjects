package com.payflow.webhook;
import com.fasterxml.jackson.databind.ObjectMapper; import org.springframework.beans.factory.annotation.Value; import org.springframework.http.*; import org.springframework.stereotype.Service; import org.springframework.web.client.RestClient;
import javax.crypto.Mac; import javax.crypto.spec.SecretKeySpec; import java.nio.charset.StandardCharsets; import java.util.*;
@Service
public class WebhookService {
 private final WebhookEndpointRepository endpoints; private final WebhookEventRepository events; private final ObjectMapper mapper; private final RestClient client=RestClient.create();
 @Value("${payflow.webhook-secret}") private String secret;
 public WebhookService(WebhookEndpointRepository e,WebhookEventRepository v,ObjectMapper m){endpoints=e;events=v;mapper=m;}
 public void publish(String merchantId,String type,Object data){
  try {String payload=mapper.writeValueAsString(Map.of("type",type,"data",data)); for(var endpoint:endpoints.findByMerchantIdAndActiveTrue(merchantId)){var event=events.save(new WebhookEvent(merchantId,type,payload));try{var response=client.post().uri(endpoint.getUrl()).header("X-PayFlow-Signature",sign(payload)).contentType(MediaType.APPLICATION_JSON).body(payload).retrieve().toBodilessEntity();event.delivered(String.valueOf(response.getStatusCode()));}catch(Exception ex){event.failed(ex.getMessage());}events.save(event);}}catch(Exception ignored){}
 }
 private String sign(String body)throws Exception{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));}
 public WebhookEndpoint register(String merchantId,String url){return endpoints.save(new WebhookEndpoint(merchantId,url));} public List<WebhookEvent> events(){return events.findTop50ByOrderByCreatedAtDesc();}
}
