package com.example.commitmate.core.interceptor;

import com.example.commitmate.core.errors.Exception401;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Object sessionUser = session.getAttribute("sessionUser");

        if (sessionUser == null) {
            // ✅ 여기서 직접 리다이렉트 하지 않고 예외를 던집니다.
            throw new Exception401("로그인이 필요한 서비스입니다.");
        }
        return true;
    }
}
