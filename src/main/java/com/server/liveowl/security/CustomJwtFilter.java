package com.server.liveowl.security;

import com.server.liveowl.util.JwtUtilHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CustomJwtFilter extends OncePerRequestFilter {
    @Autowired
    JwtUtilHelper jwtUtilHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // Bỏ qua xác thực JWT cho các endpoint login
        if (requestURI.startsWith("/users/signin") || requestURI.startsWith("/users/signup")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Thực hiện xác thực JWT cho các endpoint khác
        String token = jwtUtilHelper.getTokenFromHeader(request);

        if (token != null && jwtUtilHelper.verifyToken(token)) {
            if (token != null && jwtUtilHelper.verifyToken(token)) {
                // Trích xuất email va role từ token
                String email = jwtUtilHelper.getEmailFromToken(token);
                System.out.println("email filter " + email);
                int role = jwtUtilHelper.getRoleFromToken(token);
                System.out.println("role filter" + role);
                // Xác định danh sách quyền hạn (authorities) dựa trên role
                List<GrantedAuthority> authorities = new ArrayList<>();
                if (role == 1) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_GIAO_VIEN"));
                } else if (role == 2) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_HOC_SINH"));
                }
                // Tạo đối tượng UsernamePasswordAuthenticationToken với email và quyền hạn (roles)
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                // Thiết lập đối tượng vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                System.out.println("Valid token");
            } else {

                System.err.println("Invalid token");

            }

            filterChain.doFilter(request, response);
        }

    }
}
