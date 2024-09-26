package com.server.liveowl.security;

import ch.qos.logback.core.util.StringUtil;
import com.server.liveowl.ustil.JwtUstilHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class CustomJwtFilter extends OncePerRequestFilter {
    @Autowired
    JwtUstilHelper jwtUstilHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // Bỏ qua xác thực JWT cho các endpoint login
        if (requestURI.startsWith("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Thực hiện xác thực JWT cho các endpoint khác
        String token = getTokenFromHeader(request);

        if (token != null && jwtUstilHelper.verifyToken(token)) {
            // Trích xuất email từ token
            String email = jwtUstilHelper.getEmailFromToken(token);
            // Tạo đối tượng UsernamePasswordAuthenticationToken với email và quyền hạn (roles)
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());

            // Thiết lập đối tượng vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromHeader(HttpServletRequest request )
    {
        String header = request.getHeader("Authorization");
        String token = null;
        if(header != null || header.startsWith("Bearer "))
        {
            token = header.substring(7);

        }
        return token;
    }
}
