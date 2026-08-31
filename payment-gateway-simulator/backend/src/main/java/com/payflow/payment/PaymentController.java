package com.payflow.payment;
import com.payflow.refund.Refund; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.data.domain.Page; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.util.Map; import org.springframework.security.core.Authentication;
@RestController @RequestMapping("/api/v1/payments")
public class PaymentController {
 private final PaymentService service; public PaymentController(PaymentService s){service=s;}
 @PostMapping public ResponseEntity<Payment> create(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody PaymentRequest request,Authentication auth){var owned=new PaymentRequest(merchant(auth),request.amount(),request.currency(),request.method(),request.description(),request.customerEmail());return ResponseEntity.status(HttpStatus.CREATED).body(service.create(key,owned));}
 @GetMapping("/{id}") public Payment get(@PathVariable String id,Authentication auth){return service.getOwned(id,merchant(auth));}
 @GetMapping public Page<Payment> list(@RequestParam(required=false)String merchantId,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size,Authentication auth){return service.list(merchant(auth),page,size);}
 @PostMapping("/{id}/authorize") public Payment authorize(@PathVariable String id,@RequestBody(required=false) Map<String,Boolean> body,Authentication auth){service.getOwned(id,merchant(auth));return service.authorize(id,body!=null&&Boolean.TRUE.equals(body.get("forceSuccess")));}
 @PostMapping("/{id}/capture") public Payment capture(@PathVariable String id,Authentication auth){service.getOwned(id,merchant(auth));return service.capture(id);}
 @PostMapping("/{id}/retry") public Payment retry(@PathVariable String id,Authentication auth){service.getOwned(id,merchant(auth));return service.authorize(id,false);}
 @PostMapping("/{id}/refunds") public Refund refund(@PathVariable String id,@Valid @RequestBody RefundRequest r,Authentication auth){service.getOwned(id,merchant(auth));return service.refund(id,r.amount(),r.reason());}
 private String merchant(Authentication auth){return (String)auth.getDetails();}
 public record RefundRequest(@NotNull @DecimalMin("0.01") BigDecimal amount,String reason){}
}
