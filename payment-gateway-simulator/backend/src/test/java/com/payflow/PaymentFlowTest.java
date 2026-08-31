package com.payflow;
import com.payflow.payment.*; import org.junit.jupiter.api.Test; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.context.SpringBootTest; import org.springframework.test.context.ActiveProfiles; import java.math.BigDecimal; import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:testdb","spring.jpa.hibernate.ddl-auto=create-drop"})
class PaymentFlowTest { @Autowired PaymentService service;
 @Test void idempotencyAndLifecycle(){var r=new PaymentRequest("m_test",new BigDecimal("100.00"),"INR","card","test","a@b.com");var one=service.create("same-key",r);var two=service.create("same-key",r);assertThat(two.getId()).isEqualTo(one.getId());service.authorize(one.getId(),true);assertThat(service.capture(one.getId()).getStatus()).isEqualTo(PaymentStatus.CAPTURED);assertThat(service.refund(one.getId(),new BigDecimal("25.00"),"test").getAmount()).isEqualByComparingTo("25.00");}
}
