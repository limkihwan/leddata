package co.kr.leddata.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminLoginController {
    
    // 기본 관리자 계정 (실제 운영에서는 데이터베이스나 설정 파일에서 관리)
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "leddata2024";
    
    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        // 이미 로그인된 경우 대시보드로 리다이렉트
        if (isLoggedIn(session)) {
            return "redirect:/admin/dashboard";
        }
        return "admin/login";
    }
    
    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            // 로그인 성공
            session.setAttribute("adminLoggedIn", true);
            session.setAttribute("adminUsername", username);
            session.setMaxInactiveInterval(3600); // 1시간 세션 유지
            
            return "redirect:/admin/dashboard";
        } else {
            // 로그인 실패
            redirectAttributes.addFlashAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/admin/login";
        }
    }
    
    // 로그아웃
    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "로그아웃되었습니다.");
        return "redirect:/admin/login";
    }
    
    // 대시보드 (로그인 필요)
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/admin/login";
        }
        
        model.addAttribute("adminUsername", session.getAttribute("adminUsername"));
        return "admin/dashboard";
    }
    
    // 로그인 상태 확인
    private boolean isLoggedIn(HttpSession session) {
        Boolean loggedIn = (Boolean) session.getAttribute("adminLoggedIn");
        return loggedIn != null && loggedIn;
    }
}
