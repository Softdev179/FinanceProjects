package com.payflow.settlement;
import com.payflow.payment.*; import org.springframework.web.bind.annotation.*; import java.math.BigDecimal; import java.util.*;
@RestController @RequestMapping("/api/v1/settlements")
public class SettlementController {
 private final SettlementRepository settlements; private final PaymentRepository payments; public SettlementController(SettlementRepository s,PaymentRepository p){settlements=s;payments=p;}
 @PostMapping public Settlement create(@RequestBody Request r){BigDecimal total=payments.findAll().stream().filter(p->p.getMerchantId().equals(r.merchantId())&&(p.getStatus()==PaymentStatus.CAPTURED||p.getStatus()==PaymentStatus.PARTIALLY_REFUNDED)).map(p->p.getAmount().subtract(p.getRefundedAmount())).reduce(BigDecimal.ZERO,BigDecimal::add);return settlements.save(new Settlement(r.merchantId(),total));}
 @GetMapping public List<Settlement> list(@RequestParam String merchantId){return settlements.findByMerchantIdOrderByCreatedAtDesc(merchantId);} public record Request(String merchantId){}
}
