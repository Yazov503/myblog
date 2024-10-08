package com.liu.myblog.controller;

import com.liu.myblog.util.TokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private TokenUtil tokenUtil;

    @GetMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestHeader("token") String token) {
        if (token != null && tokenUtil.isTokenExpiringSoon(token)) {
            String newToken = tokenUtil.generateToken(tokenUtil.getIdFromToken(token), tokenUtil.getIsAdminFromToken(token));
            return ResponseEntity.ok(newToken);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid or expired");
    }
}
