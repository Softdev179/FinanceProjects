package com.wealth.platform;
import com.fasterxml.jackson.annotation.JsonIgnore; import jakarta.persistence.*;
@Entity @Table(name="app_users") public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(unique=true, nullable=false) public String email;
 @JsonIgnore @Column(nullable=false) public String passwordHash;
 public User() {} public User(String email,String passwordHash){this.email=email;this.passwordHash=passwordHash;}
}
