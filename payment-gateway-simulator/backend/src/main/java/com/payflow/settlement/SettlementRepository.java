package com.payflow.settlement;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface SettlementRepository extends JpaRepository<Settlement,String>{List<Settlement> findByMerchantIdOrderByCreatedAtDesc(String merchantId);}
