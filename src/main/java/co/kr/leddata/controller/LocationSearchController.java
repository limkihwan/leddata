package co.kr.leddata.controller;

import co.kr.leddata.service.RegionSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/location")
public class LocationSearchController {
    
    @Autowired
    private RegionSearchService regionSearchService;
    
    // 날씨 정보 검색 페이지
    @GetMapping("/weather")
    public String weatherSearch() {
        return "admin/location/weather-search";
    }
    
    // 대기질 정보 검색 페이지
    @GetMapping("/air")
    public String airSearch() {
        return "admin/location/air-search";
    }
    
    // 날씨 정보 API 검색
    @GetMapping("/api/weather")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchWeather(@RequestParam String query) {
        try {
            List<Map<String, Object>> results = regionSearchService.searchWeatherLocations(query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 대기질 정보 API 검색
    @GetMapping("/api/air")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchAir(@RequestParam String query) {
        try {
            List<Map<String, Object>> results = regionSearchService.searchAirStations(query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
