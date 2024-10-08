package com.liu.myblog.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Data
@ConfigurationProperties(prefix = "jwt")
public class TokenUtil {

    private String secret;

    private Long expiration;

    private String tokenName;

    private Long refresh_expiration;

    /**
     * 生成令牌
     */
    public String generateToken(Long userId, Boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("isAdmin", isAdmin);
        claims.put("create", new Date());
        return generateToken(claims);
    }

    /**
     * 从claims生成令牌
     */
    private String generateToken(Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiration);
        return Jwts.builder().setClaims(claims)
                .setExpiration(expirationDate)//设置过期时间
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public boolean isTokenExpiringSoon(String token) {
        Claims claims = getClaimsFromToken(token);
        long currentTime = System.currentTimeMillis() / 1000;
        long exp = claims.getExpiration().getTime() / 1000;
        return exp - currentTime <= 3600;
    }

    /**
     * @return 生成RefreshToken
     */
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("create", new Date());
        //设置过期时间为七天；
        Date expirationDate = new Date(System.currentTimeMillis() + refresh_expiration);
        return Jwts.builder().setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 从令牌中获取userId
     *
     * @param token 令牌
     */
    public Long getIdFromToken(String token) {
        Long id;
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) return null;
            id = Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException e) {
            id = null;
        }
        return id;
    }

    /**
     * 从令牌中获取userId
     *
     * @param token 令牌
     */
    public Boolean getIsAdminFromToken(String token) {
        Boolean isAdmin;
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) return false;
            isAdmin = (Boolean) claims.get("isAdmin");
        } catch (ExpiredJwtException e) {
            isAdmin = false;
        }
        return isAdmin;
    }


    /**
     * 从令牌中获取数据声明，
     *
     * @param token 令牌
     * @return 数据声明
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断令牌是否过期
     *
     * @param token 令牌
     * @return 是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) return true;
            long currentTime = System.currentTimeMillis() / 1000;
            long exp = claims.getExpiration().getTime() / 1000;
            return exp < currentTime;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 是否有效
     */
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    /**
     * 刷新refreshToken
     *
     * @param refreshToken 刷新令牌
     */
    public String getNewRefreshToken(String refreshToken) {
        try {
            Claims claims = getClaimsFromToken(refreshToken);
            Date expiration = claims.getExpiration();
            //如果refreshToken距过期时间不足1小时，刷新refreshToken
            if (expiration.getTime() - System.currentTimeMillis() < 1000 * 60 * 60 * 12) {
                return generateRefreshToken(getIdFromToken(refreshToken));
            }
            return refreshToken;
        } catch (Exception e) {
            return refreshToken;
        }
    }

    public Long getUserIdAndRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("token");
        Long userId = getIdFromToken(token);
        if (isTokenExpiringSoon(token)) {
            response.addHeader("Access-Control-Expose-Headers", "token");
            response.addHeader("token", generateToken(userId, getIsAdminFromToken(token)));
        }
        return userId;
    }

    public Long getUserId(HttpServletRequest request) {
        return getIdFromToken(request.getHeader("token"));
    }


}

