package com.payflow.webhook;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/v1/webhooks")
public class WebhookController {
 private final WebhookService service; public WebhookController(WebhookService s){service=s;}
 @PostMapping("/endpoints") public WebhookEndpoint register(@Valid @RequestBody Request r){return service.register(r.merchantId(),r.url());}
 @GetMapping("/events") public List<WebhookEvent> events(){return service.events();}
 public record Request(@NotBlank String merchantId,@NotBlank @Pattern(regexp="https?://.+",message="must be an HTTP(S) URL") String url){}
}
