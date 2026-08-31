package com.payflow.auth;
import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.time.*; import java.time.temporal.ChronoUnit; import java.util.Date;
@Service public class JwtService {
 @Value("${payflow.jwt-secret}") private String secret; @Value("${payflow.jwt-expiration-hours}") private long hours; private SecretKey key(){return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));}
 public String create(MerchantUser u){Instant now=Instant.now();return Jwts.builder().subject(u.getEmail()).claim("merchantId",u.getMerchantId()).claim("role",u.getRole()).claim("businessName",u.getBusinessName()).issuedAt(Date.from(now)).expiration(Date.from(now.plus(hours,ChronoUnit.HOURS))).signWith(key()).compact();}
 public Claims parse(String token){return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();}
}
