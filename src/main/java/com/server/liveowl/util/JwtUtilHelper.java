package com.server.liveowl.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtilHelper {

    @Value("${jwt.privatekey}")
    private String privateKey;

    public String generateToken(String data, int role) {
        // Tạo SecretKey từ privateKey
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(privateKey));

        // Thêm thông tin role vào claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);  // Thêm role kiểu int vào token

        // Xây dựng token với claims
        String jws = Jwts.builder()
                .setClaims(claims)          // Đặt claims (chứa role)
                .setSubject(data)            // Đặt subject (thông tin như email hoặc username)
                .signWith(key)               // Ký với SecretKey
                .compact();

        return jws;
    }
    public boolean verifyToken(String token)
    {
        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(privateKey));
             Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parse(token);
            return true;
        } catch (Exception e) {
           return  false;
        }
    }

    public String getEmailFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(privateKey));

        // Trích xuất claims từ token
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();  // Trả về subject (email)
    }

    public int getRoleFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(privateKey));

        // Trích xuất claims từ token
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role", Integer.class);  // Trả về role dưới dạng int
    }
}
