package com.payflow.payment;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PaymentRepository extends JpaRepository<Payment,String> {
  Optional<Payment> findByIdempotencyKey(String key);
  Page<Payment> findByMerchantId(String merchantId, Pageable pageable);
  java.util.List<Payment> findAllByMerchantId(String merchantId);
  long countByStatus(PaymentStatus status);
}
