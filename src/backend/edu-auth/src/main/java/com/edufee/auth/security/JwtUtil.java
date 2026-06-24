package com.edufee.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 负责JWT令牌的生成、解析和验证
 * 使用HMAC-SHA256签名算法，密钥从配置文件读取
 */
@Slf4j
@Component
public class JwtUtil {

    /** JWT签名密钥 */
    @Value("${jwt.secret:edufee-ms-secret-key-must-be-at-least-256-bits-long-for-hs256}")
    private String secret;

    /** 访问令牌过期时间（秒），默认24小时 */
    @Value("${jwt.expiration:86400}")
    private long expiration;

    /** 令牌签发者 */
    private static final String ISSUER = "EduFeeMS";

    /**
     * 获取签名密钥
     * 自动将字符串密钥转换为HMAC-SHA256所需的SecretKey
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT访问令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roleCode 角色编码
     * @return JWT令牌字符串
     */
    public String generateToken(Long userId, String username, String roleCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roleCode", roleCode);

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuer(ISSUER)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从令牌中解析Claims
     *
     * @param token JWT令牌
     * @return Claims对象
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中获取用户名（subject）
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Object userId = parseToken(token).get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从令牌中获取角色编码
     *
     * @param token JWT令牌
     * @return 角色编码
     */
    public String getRoleCodeFromToken(String token) {
        return (String) parseToken(token).get("roleCode");
    }

    /**
     * 验证令牌是否有效
     * 验证签名是否正确、是否过期
     *
     * @param token JWT令牌
     * @return true-有效 false-无效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT令牌已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT令牌: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT令牌格式错误: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("JWT签名验证失败: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT令牌为空或非法参数: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 获取令牌过期时间（秒）
     */
    public long getExpiration() {
        return expiration;
    }

}
