package com.wealth.platform;
import com.fasterxml.jackson.annotation.JsonIgnore; import jakarta.persistence.*; import java.math.BigDecimal;
@Entity public class PortfolioItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @JsonIgnore @ManyToOne(optional=false) public User owner;
 @Enumerated(EnumType.STRING) public AssetType type;
 public String name; @Column(precision=19,scale=2) public BigDecimal value; @Column(precision=19,scale=2) public BigDecimal liability = BigDecimal.ZERO;
 public enum AssetType { BANK_ACCOUNT,CREDIT_CARD,STOCK,MUTUAL_FUND,FIXED_DEPOSIT,LOAN }
}
