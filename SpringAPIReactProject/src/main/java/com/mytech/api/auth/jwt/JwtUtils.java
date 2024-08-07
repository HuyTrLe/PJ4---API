package com.mytech.api.auth.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.mytech.api.auth.services.MyUserDetails;
import com.mytech.api.config.OAuth2.AppProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  private AppProperties appProperties;

  public JwtUtils(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  // @Value("${com.mytech.api.jwtSecret}")
  // private String jwtSecret;

  // @Value("${com.mytech.api.jwtExpirationMs}")
  // private int jwtExpirationMs;

  // public String generateJwtToken(Authentication authentication) {
  // MyUserDetails userPrincipal = (MyUserDetails) authentication.getPrincipal();
  // Date now = new Date();
  // Date expiryDate = new Date(now.getTime() +
  // appProperties.getAuth().getTokenExpirationMsec());
  // return Jwts.builder()
  // .setSubject(userPrincipal.getEmail())
  // .setIssuedAt(new Date())
  // .setExpiration(expiryDate)
  // .signWith(key(), SignatureAlgorithm.HS256)
  // .compact();
  // }

  // private Key key() {
  // return
  // Keys.hmacShaKeyFor(Decoders.BASE64.decode(appProperties.getAuth().getTokenSecret()));
  // }

  public String createToken(Authentication authentication) {
    MyUserDetails userPrincipal = (MyUserDetails) authentication.getPrincipal();
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());
    SecretKey secretKey = Keys.hmacShaKeyFor(appProperties.getAuth().getTokenSecret().getBytes());
    return Jwts.builder()
        .setSubject(Long.toString(userPrincipal.getId()))
        .setIssuedAt(new Date())
        .setExpiration(expiryDate)
        .signWith(secretKey, SignatureAlgorithm.HS512)
        .compact();
  }

  public Long getUserIdFromToken(String token) {
    SecretKey secretKey = Keys.hmacShaKeyFor(appProperties.getAuth().getTokenSecret().getBytes());

    Claims claims = Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody();

    return Long.parseLong(claims.getSubject());
  }

  // public String getEmailFromJwtToken(String token) {
  // return Jwts.parserBuilder().setSigningKey(key()).build()
  // .parseClaimsJws(token).getBody().getSubject(); // Lấy Subject thay vì "email"
  // }

  public boolean validateJwtToken(String authToken) {
    SecretKey secretKey = Keys.hmacShaKeyFor(appProperties.getAuth().getTokenSecret().getBytes());
    try {
      Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(authToken);
      return true;
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    }
    return false;
  }
}