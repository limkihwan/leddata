package co.kr.leddata.controller;

import co.kr.leddata.entity.RegionGridMapping;
import co.kr.leddata.entity.AirStation;
import co.kr.leddata.service.RegionSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/search/api")
public class RegionSearchController {
    
    @Autowired
    private RegionSearchService regionSearchService;
    
    /**
     * 통합 검색 API
     */
    @GetMapping("/all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchAll(@RequestParam String keyword) {
        Map<String, Object> result = regionSearchService.searchAll(keyword);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 지역명 검색 API
     */
    @GetMapping("/regions")
    @ResponseBody
    public ResponseEntity<List<RegionGridMapping>> searchRegions(@RequestParam String keyword) {
        List<RegionGridMapping> regions = regionSearchService.searchRegionByName(keyword);
        return ResponseEntity.ok(regions);
    }
    
    /**
     * 측정소 검색 API
     */
    @GetMapping("/stations")
    @ResponseBody
    public ResponseEntity<List<AirStation>> searchStations(@RequestParam String keyword) {
        List<AirStation> stations = regionSearchService.searchAirStationByName(keyword);
        return ResponseEntity.ok(stations);
    }
    
    /**
     * 플레이어 설정 추천 API
     */
    @GetMapping("/suggest")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> suggestPlayerConfig(@RequestParam String keyword) {
        Map<String, Object> suggestion = regionSearchService.generatePlayerConfigSuggestion(keyword);
        return ResponseEntity.ok(suggestion);
    }
    
    /**
     * 시/도 목록 API
     */
    @GetMapping("/provinces")
    @ResponseBody
    public ResponseEntity<List<String>> getProvinces() {
        List<String> provinces = regionSearchService.getAllProvinces();
        return ResponseEntity.ok(provinces);
    }
    
    /**
     * 시/군/구 목록 API
     */
    @GetMapping("/cities")
    @ResponseBody
    public ResponseEntity<List<String>> getCities(@RequestParam String province) {
        List<String> cities = regionSearchService.getCitiesByProvince(province);
        return ResponseEntity.ok(cities);
    }
    
    /**
     * 지역별 상세 검색 API
     */
    @GetMapping("/regions/{province}")
    @ResponseBody
    public ResponseEntity<List<RegionGridMapping>> getRegionsByProvince(@PathVariable String province) {
        List<RegionGridMapping> regions = regionSearchService.getRegionsByProvince(province);
        return ResponseEntity.ok(regions);
    }
}