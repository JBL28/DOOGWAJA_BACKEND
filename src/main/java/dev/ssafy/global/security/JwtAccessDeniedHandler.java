package dev.ssafy.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ssafy.global.exception.ErrorCode;
import dev.ssafy.global.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        ErrorResponse errorResponse = new ErrorResponse("접근 권한이 없습니다", ErrorCode.ACCESS_DENIED.getCode());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
