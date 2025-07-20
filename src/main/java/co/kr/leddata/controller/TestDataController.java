package co.kr.leddata.controller;

import co.kr.leddata.service.RegionSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/test")
public class TestDataController {
    
    @Autowired
    private RegionSearchService regionSearchService;
    
    @GetMapping("/search")
    @ResponseBody
    public Map<String, Object> testSearch() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 날씨 검색 테스트
            var weatherResults = regionSearchService.searchWeatherLocations("서울");
            result.put("weatherResults", weatherResults);
            result.put("weatherCount", weatherResults.size());
            
            // 대기질 검색 테스트
            var airResults = regionSearchService.searchAirStations("중구");
            result.put("airResults", airResults);
            result.put("airCount", airResults.size());
            
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
