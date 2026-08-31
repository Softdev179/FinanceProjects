package com.payflow.refund;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/refunds")
public class RefundController {
  private final RefundRepository refunds;
  public RefundController(RefundRepository refunds) { this.refunds = refunds; }

  @GetMapping
  public List<Refund> list(@RequestParam(required = false) String paymentId) {
    return paymentId == null ? refunds.findAll() : refunds.findByPaymentId(paymentId);
  }
}
