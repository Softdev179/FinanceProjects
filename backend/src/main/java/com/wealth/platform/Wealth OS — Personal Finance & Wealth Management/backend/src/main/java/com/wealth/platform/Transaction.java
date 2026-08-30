package com.wealth.platform;
import com.fasterxml.jackson.annotation.JsonIgnore; import jakarta.persistence.*; import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="transactions") public class Transaction {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @JsonIgnore @ManyToOne(optional=false) public User owner; public LocalDate date;
 @Enumerated(EnumType.STRING) public Direction direction; public String category; public String description;
 @Column(precision=19,scale=2) public BigDecimal amount;
 public enum Direction { INCOME, EXPENSE }
}
