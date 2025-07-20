package co.kr.leddata.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // 로그인 페이지와 로그인 처리는 제외
        if (requestURI.equals("/") ||
            requestURI.equals("/admin/login") || 
            requestURI.equals("/admin/logout") ||
            requestURI.startsWith("/api/") ||
            requestURI.startsWith("/player/") ||
            requestURI.startsWith("/h2-console") ||
            requestURI.startsWith("/static/") ||
            !requestURI.startsWith("/admin/")) {
            return true;
        }
        
        HttpSession session = request.getSession();
        Boolean loggedIn = (Boolean) session.getAttribute("adminLoggedIn");
        
        if (loggedIn == null || !loggedIn) {
            response.sendRedirect("/admin/login");
            return false;
        }
        
        return true;
    }
}
