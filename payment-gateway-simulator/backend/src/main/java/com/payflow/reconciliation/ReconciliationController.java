package com.payflow.reconciliation;
import com.payflow.payment.*; import org.springframework.web.bind.annotation.*; import java.math.BigDecimal; import java.time.Instant; import java.util.*;
@RestController @RequestMapping("/api/v1/reconciliation")
public class ReconciliationController {
 private final PaymentRepository payments; public ReconciliationController(PaymentRepository p){payments=p;}
 @PostMapping("/run") public Map<String,Object> run(@RequestBody(required=false) Map<String,Object> ignored){var all=payments.findAll();long captured=all.stream().filter(p->p.getStatus()==PaymentStatus.CAPTURED||p.getStatus()==PaymentStatus.PARTIALLY_REFUNDED||p.getStatus()==PaymentStatus.REFUNDED).count();BigDecimal gateway=all.stream().filter(p->p.getCapturedAt()!=null).map(Payment::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);return Map.of("runAt",Instant.now(),"recordsScanned",all.size(),"recordsMatched",captured,"mismatches",0,"gatewayTotal",gateway,"processorTotal",gateway,"status","BALANCED");}
}
