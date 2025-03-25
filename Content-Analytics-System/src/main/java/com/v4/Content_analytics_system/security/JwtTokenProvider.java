package com.v4.Content_analytics_system.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        System.out.println("Generating token for user: " + userDetails.getUsername());
        System.out.println("Token will expire at: " + expiryDate);

        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .compact();

        System.out.println("Generated token: " + token.substring(0, Math.min(10, token.length())) + "...");
        return token;
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            System.out.println("Extracted username from token: " + username);
            return username;
        } catch (Exception e) {
            System.err.println("Error extracting username from token: " + e.getMessage());
            throw e;
        }
    }

    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String roles = claims.get("roles").toString();
            System.out.println("Extracted roles from token: " + roles);

            return Arrays.stream(roles.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error extracting authorities from token: " + e.getMessage());
            throw e;
        }
    }

    public boolean validateToken(String token) {
        try {
            System.out.println("Validating token: " + (token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null"));

            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token);

            System.out.println("Token validation successful");
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token expired: " + e.getMessage());
            return false;
        } catch (JwtException e) {
            System.err.println("JWT validation error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Token validation error: " + e.getMessage());
            return false;
        }
    }
}