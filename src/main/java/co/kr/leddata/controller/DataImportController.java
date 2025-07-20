package co.kr.leddata.controller;

import co.kr.leddata.service.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/data")
public class DataImportController {
    
    private static final Logger log = LoggerFactory.getLogger(DataImportController.class);
    
    @Autowired
    private DataImportService dataImportService;
    
    @GetMapping("/import")
    public String importPage(Model model) {
        // 현재 데이터 통계 조회
        Map<String, Object> stats = dataImportService.getDataStatistics();
        model.addAttribute("stats", stats);
        return "admin/data-import";
    }
    
    /**
     * 엑셀 파일로 행정구역 코드 업로드
     */
    @PostMapping("/upload/regions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadRegionExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "파일이 선택되지 않았습니다.");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 파일 확장자 검증
            String fileName = file.getOriginalFilename();
            if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
                result.put("success", false);
                result.put("message", "엑셀 파일(.xlsx, .xls)만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(result);
            }
            
            log.info("행정구역 코드 엑셀 파일 업로드 시작: {}", fileName);
            
            // 데이터 처리
            Map<String, Object> importResult = dataImportService.importRegionFromExcel(file);
            
            result.put("success", true);
            result.put("message", "행정구역 코드 업로드가 완료되었습니다.");
            result.putAll(importResult);
            
            log.info("행정구역 코드 업로드 완료: {}개 처리", importResult.get("processedCount"));
            
        } catch (Exception e) {
            log.error("행정구역 코드 업로드 실패", e);
            result.put("success", false);
            result.put("message", "업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 공공데이터 API로 대기질 측정소 파싱
     */
    @PostMapping("/parse/airstations")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> parseAirStations() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("대기질 측정소 API 파싱 시작");
            
            // 비동기 처리
            Map<String, Object> parseResult = dataImportService.parseAirStationsFromApi();
            
            result.put("success", true);
            result.put("message", "대기질 측정소 파싱이 완료되었습니다.");
            result.putAll(parseResult);
            
            log.info("대기질 측정소 파싱 완료: {}개 처리", parseResult.get("totalStations"));
            
        } catch (Exception e) {
            log.error("대기질 측정소 파싱 실패", e);
            result.put("success", false);
            result.put("message", "파싱 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 테스트 데이터 생성
     */
    @PostMapping("/create/sample")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createSampleData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("테스트 데이터 생성 시작");
            
            Map<String, Object> sampleResult = dataImportService.createSampleData();
            
            result.put("success", true);
            result.put("message", "테스트 데이터가 생성되었습니다.");
            result.putAll(sampleResult);
            
        } catch (Exception e) {
            log.error("테스트 데이터 생성 실패", e);
            result.put("success", false);
            result.put("message", "테스트 데이터 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 데이터 초기화
     */
    @PostMapping("/reset/all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetAllData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("전체 데이터 초기화 시작");
            
            dataImportService.resetAllData();
            
            result.put("success", true);
            result.put("message", "전체 데이터가 초기화되었습니다.");
            
        } catch (Exception e) {
            log.error("데이터 초기화 실패", e);
            result.put("success", false);
            result.put("message", "데이터 초기화 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 데이터 통계 조회 API
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDataStats() {
        try {
            Map<String, Object> stats = dataImportService.getDataStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("데이터 통계 조회 실패", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "통계 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.ok(errorResult);
        }
    }
}