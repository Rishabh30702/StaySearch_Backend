package com.example.StaySearch.StaySearchBackend.JWT;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    /**
     * @param token The JWT token from the request
     * @param username The username from the user details
     * @param lastPasswordResetDate The timestamp from your DB (in milliseconds)
     */
    public boolean validateToken(String token, String username, long lastPasswordResetDate) {
        final String tokenUsername = extractUsername(token);
        final Date issuedAt = extractIssuedAt(token);

        // 1. Check if username matches
        // 2. Check if token is physically expired (TTL)
        // 3. Check if token was issued BEFORE the last password change
        return (tokenUsername.equals(username) &&
                !isTokenExpired(token) &&
                !isIssuedBeforePasswordChange(issuedAt, lastPasswordResetDate));
    }

    private boolean isIssuedBeforePasswordChange(Date issuedAt, long lastPasswordResetDate) {
        // If issuedAt is null, something is wrong with the token
        if (issuedAt == null) return true;

        // Check if the token was issued before the password was changed
        // We use milliseconds for a precise comparison
        return issuedAt.getTime() < lastPasswordResetDate;
    }

    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
    public String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Removes "Bearer " prefix
        }
        return null;
    }
}
