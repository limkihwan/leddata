package co.kr.leddata.controller;

import co.kr.leddata.entity.Player;
import co.kr.leddata.entity.PlayerSession;
import co.kr.leddata.service.PlayerService;
import co.kr.leddata.service.RegionSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/players")
public class PlayerAdminController {
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private RegionSearchService regionSearchService;
    
    // 플레이어 목록 페이지
    @GetMapping
    public String playerList(Model model) {
        List<Player> players = playerService.getAllPlayers();
        model.addAttribute("players", players);
        return "admin/players/list";
    }
    
    // 플레이어 등록 페이지
    @GetMapping("/new")
    public String newPlayerForm(Model model) {
        model.addAttribute("player", new Player());
        model.addAttribute("themes", getAvailableThemes());
        return "admin/players/form";
    }
    
    // 플레이어 수정 페이지
    @GetMapping("/{playerCode}/edit")
    public String editPlayerForm(@PathVariable String playerCode, Model model) {
        Optional<Player> playerOpt = playerService.getPlayerByCode(playerCode);
        if (!playerOpt.isPresent()) {
            return "redirect:/admin/players?error=notfound";
        }
        
        model.addAttribute("player", playerOpt.get());
        model.addAttribute("themes", getAvailableThemes());
        model.addAttribute("isEdit", true);
        return "admin/players/form";
    }
    
    // 플레이어 저장
    @PostMapping
    public String savePlayer(@ModelAttribute Player player, RedirectAttributes redirectAttributes) {
        try {
            // 플레이어 코드 유효성 검사
            if (player.getPlayerCode() == null || player.getPlayerCode().length() != 6) {
                redirectAttributes.addFlashAttribute("error", "플레이어 코드는 6자리 숫자여야 합니다.");
                return "redirect:/admin/players/new";
            }
            
            // 코드 중복 검사 (신규 등록 시)
            if (player.getCreatedAt() == null) {
                Optional<Player> existing = playerService.getPlayerByCode(player.getPlayerCode());
                if (existing.isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "이미 존재하는 플레이어 코드입니다.");
                    return "redirect:/admin/players/new";
                }
            }
            
            playerService.savePlayer(player);
            redirectAttributes.addFlashAttribute("success", "플레이어가 성공적으로 저장되었습니다.");
            return "redirect:/admin/players";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "저장 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/admin/players/new";
        }
    }
    
    // 플레이어 삭제
    @PostMapping("/{playerCode}/delete")
    public String deletePlayer(@PathVariable String playerCode, RedirectAttributes redirectAttributes) {
        try {
            playerService.deletePlayer(playerCode);
            redirectAttributes.addFlashAttribute("success", "플레이어가 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/players";
    }
    
    // 접속 권한 토글
    @PostMapping("/{playerCode}/toggle-access")
    public String togglePlayerAccess(@PathVariable String playerCode, RedirectAttributes redirectAttributes) {
        try {
            playerService.togglePlayerAccess(playerCode);
            redirectAttributes.addFlashAttribute("success", "접속 권한이 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "권한 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/players";
    }
    
    // 플레이어 세션 관리 페이지
    @GetMapping("/{playerCode}/sessions")
    public String playerSessions(@PathVariable String playerCode, Model model) {
        Optional<Player> playerOpt = playerService.getPlayerByCode(playerCode);
        if (!playerOpt.isPresent()) {
            return "redirect:/admin/players?error=notfound";
        }
        
        List<PlayerSession> sessions = playerService.getPlayerSessions(playerCode);
        model.addAttribute("player", playerOpt.get());
        model.addAttribute("sessions", sessions);
        return "admin/players/sessions";
    }
    
    // 세션 활성화
    @PostMapping("/sessions/{sessionId}/activate")
    public String activateSession(@PathVariable String sessionId, RedirectAttributes redirectAttributes) {
        try {
            playerService.activateSession(sessionId);
            redirectAttributes.addFlashAttribute("success", "세션이 활성화되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "세션 활성화 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/players";
    }
    
    // 세션 차단
    @PostMapping("/sessions/{sessionId}/block")
    public String blockSession(@PathVariable String sessionId, RedirectAttributes redirectAttributes) {
        try {
            playerService.blockSession(sessionId);
            redirectAttributes.addFlashAttribute("success", "세션이 차단되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "세션 차단 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/players";
    }
    
    // 사용 가능한 테마 목록 (추후 파일 시스템에서 읽어올 수 있음)
    private List<String> getAvailableThemes() {
        return List.of("default", "dark", "blue", "green", "red", "custom");
    }
}
