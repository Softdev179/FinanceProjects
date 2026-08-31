package com.payflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PayFlowApplication {
  public static void main(String[] args) { SpringApplication.run(PayFlowApplication.class, args); }
}
