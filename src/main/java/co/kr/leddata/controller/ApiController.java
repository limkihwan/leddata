package co.kr.leddata.controller;

import co.kr.leddata.entity.WeatherData;
import co.kr.leddata.entity.AirData;
import co.kr.leddata.repository.WeatherRepository;
import co.kr.leddata.repository.AirRepository;
import co.kr.leddata.service.DataCollectorService;
import co.kr.leddata.service.WeatherService;
import co.kr.leddata.service.AirService;
import co.kr.leddata.service.ForestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    @Autowired
    private WeatherRepository weatherRepository;
    
    @Autowired 
    private AirRepository airRepository;
    
    @Autowired
    private DataCollectorService dataCollectorService;
    
    @Autowired
    private WeatherService weatherService;
    
    @Autowired
    private AirService airService;
    
    @Autowired
    private ForestService forestService;
    
    @GetMapping("/weather/{playerCode}")
    public ResponseEntity<WeatherData> getWeatherData(@PathVariable String playerCode) {
        Optional<WeatherData> weather = weatherRepository.findByPlayerCode(playerCode);
        
        if (weather.isPresent()) {
            return ResponseEntity.ok(weather.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/air/{playerCode}")
    public ResponseEntity<AirData> getAirData(@PathVariable String playerCode) {
        Optional<AirData> air = airRepository.findByPlayerCode(playerCode);
        
        if (air.isPresent()) {
            return ResponseEntity.ok(air.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/weather")
    public ResponseEntity<List<WeatherData>> getAllWeatherData() {
        List<WeatherData> weatherList = weatherRepository.findAll();
        return ResponseEntity.ok(weatherList);
    }
    
    @GetMapping("/air")
    public ResponseEntity<List<AirData>> getAllAirData() {
        List<AirData> airList = airRepository.findAll();
        return ResponseEntity.ok(airList);
    }
    
    @PostMapping("/collect")
    public ResponseEntity<Map<String, String>> collectDataNow() {
        try {
            dataCollectorService.collectAllDataNow();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "데이터 수집이 완료되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "데이터 수집 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        long weatherCount = weatherRepository.count();
        long airCount = airRepository.count();
        
        status.put("service", "LED 환경정보 서비스");
        status.put("status", "정상 작동중");
        status.put("weatherDataCount", weatherCount);
        status.put("airDataCount", airCount);
        status.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/test/weather/{nx}/{ny}")
    public ResponseEntity<Map<String, Object>> testWeatherAPI(@PathVariable Integer nx, @PathVariable Integer ny) {
        try {
            WeatherData weatherData = weatherService.getWeatherData("TEST", nx, ny, "테스트지역");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", weatherData);
            result.put("message", "기상청 API 테스트 성공");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "기상청 API 테스트 실패");
            
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    @GetMapping("/test/air/{stationName}")
    public ResponseEntity<Map<String, Object>> testAirAPI(@PathVariable String stationName) {
        try {
            AirData airData = airService.getAirData("TEST", stationName, "테스트지역");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", airData);
            result.put("message", "대기질 API 테스트 성공");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "대기질 API 테스트 실패");
            
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    @GetMapping("/test/forest/{regionCode}")
    public ResponseEntity<Map<String, Object>> testForestAPI(@PathVariable String regionCode) {
        try {
            Map<String, Object> forestData = forestService.getForestFireData("TEST", regionCode, "테스트지역");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", forestData);
            result.put("message", "산림청 API 테스트 성공");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "산림청 API 테스트 실패");
            
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    @GetMapping("/data/{playerCode}")
    public ResponseEntity<Map<String, Object>> getAllPlayerData(@PathVariable String playerCode) {
        Map<String, Object> data = new HashMap<>();
        
        Optional<WeatherData> weather = weatherRepository.findByPlayerCode(playerCode);
        Optional<AirData> air = airRepository.findByPlayerCode(playerCode);
        
        data.put("playerCode", playerCode);
        data.put("weather", weather.orElse(null));
        data.put("air", air.orElse(null));
        data.put("hasData", weather.isPresent() && air.isPresent());
        
        return ResponseEntity.ok(data);
    }
}