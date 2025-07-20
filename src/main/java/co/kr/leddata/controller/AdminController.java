package co.kr.leddata.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @GetMapping("/config")
    public String showConfigPage(Model model) {
        try {
            Map<String, Object> playerConfigs = loadPlayerConfigs();
            model.addAttribute("configs", playerConfigs);
            return "admin/config";
        } catch (Exception e) {
            model.addAttribute("error", "설정 파일을 읽을 수 없습니다: " + e.getMessage());
            return "admin/config";
        }
    }
    
    @GetMapping("/api/config")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getConfigs() {
        try {
            Map<String, Object> playerConfigs = loadPlayerConfigs();
            return ResponseEntity.ok(playerConfigs);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/guide")
    public String showGuide() {
        return "admin/guide";
    }
    
    @GetMapping("/search")
    public String showSearchPage() {
        return "admin/search";
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadPlayerConfigs() throws IOException {
        ClassPathResource resource = new ClassPathResource("player-config.json");
        JsonNode rootNode = objectMapper.readTree(resource.getInputStream());
        return objectMapper.convertValue(rootNode, Map.class);
    }
}