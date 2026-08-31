package com.payflow.payment;
import com.payflow.common.ApiException; import com.payflow.refund.*; import com.payflow.webhook.WebhookService;
import org.springframework.dao.DataIntegrityViolationException; import org.springframework.data.domain.*; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.util.concurrent.ThreadLocalRandom;
@Service
public class PaymentService {
 private final PaymentRepository payments; private final RefundRepository refunds; private final WebhookService webhooks;
 public PaymentService(PaymentRepository p,RefundRepository r,WebhookService w){payments=p;refunds=r;webhooks=w;}
 @Transactional public Payment create(String key,PaymentRequest request){
  if(key==null||key.isBlank())throw new ApiException(HttpStatus.BAD_REQUEST,"Idempotency-Key header is required");
  var existing=payments.findByIdempotencyKey(key); if(existing.isPresent()){validateSame(existing.get(),request);return existing.get();}
  try{return payments.saveAndFlush(new Payment(key,request));}catch(DataIntegrityViolationException e){return payments.findByIdempotencyKey(key).orElseThrow();}
 }
 private void validateSame(Payment p,PaymentRequest r){if(p.getAmount().compareTo(r.amount())!=0||!p.getCurrency().equalsIgnoreCase(r.currency())||!p.getMerchantId().equals(r.merchantId()))throw new ApiException(HttpStatus.CONFLICT,"Idempotency key was already used with different request data");}
 public Payment get(String id){return payments.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Payment not found"));}
 public Payment getOwned(String id,String merchantId){var payment=get(id);if(!payment.getMerchantId().equals(merchantId))throw new ApiException(HttpStatus.NOT_FOUND,"Payment not found");return payment;}
 public Page<Payment> list(String merchantId,int page,int size){var sort=PageRequest.of(page,Math.min(size,100),Sort.by("createdAt").descending());return merchantId==null?payments.findAll(sort):payments.findByMerchantId(merchantId,sort);}
 @Transactional public Payment authorize(String id,boolean forceSuccess){var p=get(id);if(p.getStatus()!=PaymentStatus.CREATED&&p.getStatus()!=PaymentStatus.FAILED)throw invalid(p,"authorize");boolean success=forceSuccess||ThreadLocalRandom.current().nextInt(100)<82;p.authorize(success,success?null:"BANK_DECLINED: simulated issuer decline");payments.save(p);webhooks.publish(p.getMerchantId(),success?"payment.authorized":"payment.failed",p);return p;}
 @Transactional public Payment capture(String id){var p=get(id);if(p.getStatus()!=PaymentStatus.AUTHORIZED)throw invalid(p,"capture");p.capture();payments.save(p);webhooks.publish(p.getMerchantId(),"payment.captured",p);return p;}
 @Transactional public Refund refund(String id,BigDecimal amount,String reason){var p=get(id);if(p.getStatus()!=PaymentStatus.CAPTURED&&p.getStatus()!=PaymentStatus.PARTIALLY_REFUNDED)throw invalid(p,"refund");var available=p.getAmount().subtract(p.getRefundedAmount());if(amount.signum()<=0||amount.compareTo(available)>0)throw new ApiException(HttpStatus.BAD_REQUEST,"Refund must be positive and no greater than "+available);var refund=refunds.save(new Refund(id,amount,reason==null?"Requested by merchant":reason));p.refund(amount);payments.save(p);webhooks.publish(p.getMerchantId(),"refund.created",refund);return refund;}
 private ApiException invalid(Payment p,String action){return new ApiException(HttpStatus.CONFLICT,"Cannot "+action+" payment in "+p.getStatus()+" state");}
}
