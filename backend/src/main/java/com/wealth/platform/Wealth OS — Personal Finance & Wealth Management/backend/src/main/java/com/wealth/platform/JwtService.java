package com.wealth.platform;
import io.jsonwebtoken.Jwts; import io.jsonwebtoken.security.Keys; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.util.*;
@Service public class JwtService {
 private final SecretKey key; public JwtService(@Value("${app.jwt-secret}") String secret){key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));}
 String create(User u){return Jwts.builder().subject(u.id.toString()).claim("email",u.email).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+86400000)).signWith(key).compact();}
 Long subject(String token){return Long.valueOf(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());}
}
