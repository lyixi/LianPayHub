package com.lianpayhub.service.security;

import com.lianpayhub.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecurityProperties securityProperties;

    public JwtService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String generateAdminToken(Long adminId, String username) {
        return generateAdminToken(adminId, username, null);
    }

    public String generateAdminToken(Long adminId, String username, Integer passwordVersion) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + securityProperties.getJwtExpireMinutes() * 60L * 1000L);
        return Jwts.builder()
                .setSubject(String.valueOf(adminId))
                .claim("username", username)
                .claim("passwordVersion", passwordVersion)
                .claim("type", "ADMIN")
                .setIssuedAt(now)
                .setExpiration(expireAt)
                .signWith(SignatureAlgorithm.HS256, securityProperties.getJwtSecret())
                .compact();
    }

    public String generateUserToken(Long userId, String appId, String mobile, String deviceCode,
                                    Long tokenVersion, Integer expireMinutes) {
        Date now = new Date();
        int safeExpireMinutes = expireMinutes == null || expireMinutes < 1 ? securityProperties.getJwtExpireMinutes() : expireMinutes;
        Date expireAt = new Date(now.getTime() + safeExpireMinutes * 60L * 1000L);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("appId", appId)
                .claim("mobile", mobile)
                .claim("deviceCode", deviceCode)
                .claim("tokenVersion", tokenVersion)
                .claim("type", "USER")
                .setIssuedAt(now)
                .setExpiration(expireAt)
                .signWith(SignatureAlgorithm.HS256, securityProperties.getJwtSecret())
                .compact();
    }

    public String generateUserToken(Long userId, String appId, String mobile, Long tokenVersion) {
        return generateUserToken(userId, appId, mobile, null, tokenVersion, securityProperties.getJwtExpireMinutes());
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .setSigningKey(securityProperties.getJwtSecret())
                .parseClaimsJws(token)
                .getBody();
    }
}
