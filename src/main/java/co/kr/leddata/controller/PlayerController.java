package co.kr.leddata.controller;

import co.kr.leddata.entity.WeatherData;
import co.kr.leddata.entity.AirData;
import co.kr.leddata.repository.WeatherRepository;
import co.kr.leddata.repository.AirRepository;
import co.kr.leddata.service.PlayerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import co.kr.leddata.service.DataCollectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/player")
public class PlayerController {
    
    @Autowired
    private WeatherRepository weatherRepository;
    
    @Autowired
    private AirRepository airRepository;
    
    @Autowired
    private DataCollectorService dataCollectorService;
    
    @Autowired
    private PlayerService playerService;
    
    @GetMapping("/{playerCode}")
    public String showPlayer(@PathVariable String playerCode, 
                           Model model, 
                           HttpServletRequest request,
                           HttpSession session) {
        
        try {
            // 접속 세션 관리
            String sessionId = session.getId();
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            // 플레이어 접속 시도
            String playerSessionId = playerService.attemptConnection(playerCode, ipAddress, userAgent);
            
            // 세션에 정보 저장
            session.setAttribute("playerCode", playerCode);
            session.setAttribute("playerSessionId", playerSessionId);
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "player/error";
        }
        
        // 데이터 조회
        Optional<WeatherData> weatherOpt = weatherRepository.findByPlayerCode(playerCode);
        Optional<AirData> airOpt = airRepository.findByPlayerCode(playerCode);
        
        // 데이터가 없으면 즉시 수집
        if (weatherOpt.isEmpty() || airOpt.isEmpty()) {
            dataCollectorService.collectAllDataNow();
            
            // 다시 조회
            weatherOpt = weatherRepository.findByPlayerCode(playerCode);
            airOpt = airRepository.findByPlayerCode(playerCode);
        }
        
        // 데이터가 여전히 없을 경우 기본값 설정
        WeatherData weather = weatherOpt.orElse(createDefaultWeather(playerCode));
        AirData air = airOpt.orElse(createDefaultAir(playerCode));
        
        model.addAttribute("weather", weather);
        model.addAttribute("air", air);
        model.addAttribute("playerCode", playerCode);
        
        return "player/main";  // templates/player/main.html
    }
    
    @GetMapping("/")
    public String index() {
        return "redirect:/player/240025";  // 기본 플레이어로 리다이렉트
    }
    
    @GetMapping("/refresh/{playerCode}")
    public String refreshPlayer(@PathVariable String playerCode) {
        // 해당 플레이어 데이터만 새로고침
        dataCollectorService.collectAllDataNow();
        return "redirect:/player/" + playerCode;
    }
    
    // 기본 날씨 데이터 생성
    private WeatherData createDefaultWeather(String playerCode) {
        WeatherData weather = new WeatherData(playerCode, "테스트 지역");
        weather.setTemperature(20.0);
        weather.setWeather("맑음");
        weather.setSkyState("맑음");
        weather.setHumidity(60);
        weather.setWindDirection("북풍");
        weather.setWindSpeed(2.5);
        weather.setRainProbability(10);
        weather.setSunrise("06:30");
        weather.setSunset("18:30");
        return weather;
    }
    
    // 기본 대기질 데이터 생성
    private AirData createDefaultAir(String playerCode) {
        AirData air = new AirData(playerCode, "테스트 측정소", "테스트 지역");
        air.setPm10Value(30);
        air.setPm10Grade("좋음");
        air.setPm25Value(15);
        air.setPm25Grade("좋음");
        air.setCaiValue(50);
        air.setCaiGrade("보통");
        air.setO3Value(0.030);
        air.setO3Grade("좋음");
        air.setNo2Value(0.020);
        air.setNo2Grade("좋음");
        air.setCoValue(0.5);
        air.setCoGrade("좋음");
        air.setSo2Value(0.005);
        air.setSo2Grade("좋음");
        return air;
    }
    
    // 클라이언트 IP 주소 추출
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}