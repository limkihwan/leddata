package co.kr.leddata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ForestService {
    
    private static final Logger log = LoggerFactory.getLogger(ForestService.class);
    
    @Value("${api.forest.service-key}")
    private String serviceKey;
    
    @Value("${api.forest.base-url}")
    private String baseUrl;
    
    private final RestTemplate restTemplate;
    
    public ForestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public Map<String, Object> getForestFireData(String playerCode, String regionCode, String region) {
        try {
            String provincialCode = getProvincialCode(regionCode);
            String url = buildForestUrl(provincialCode);
            log.debug("산림청 API 호출: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            log.debug("산림청 API 응답 받음");
            
            // JSON 응답 파싱 후 산불 위험도 정보 생성
            Map<String, Object> forestData = parseForestResponse(response, playerCode, region);
            
            log.info("산불 데이터 조회 성공: {} - 위험도: {}", playerCode, forestData.get("dangerLevel"));
            return forestData;
            
        } catch (Exception e) {
            log.error("산불 데이터 조회 실패: {} - {}", playerCode, e.getMessage());
            return createDefaultForestData(playerCode, region);
        }
    }
    
    private String buildForestUrl(String provincialCode) {
        LocalDateTime now = LocalDateTime.now();
        String currentDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", "10")
                .queryParam("pageNo", "1")
                .queryParam("dataType", "JSON")
                .queryParam("forestPointCd", provincialCode)
                .queryParam("searchDate", currentDate)
                .toUriString();
    }
    
    private String getProvincialCode(String regionCode) {
        // 지역 코드에서 시도코드 앞 2자리만 추출
        if (regionCode != null && regionCode.length() >= 2) {
            return regionCode.substring(0, 2);
        }
        return "11"; // 기본값: 서울 (11)
    }
    
    private Map<String, Object> parseForestResponse(String response, String playerCode, String region) {
        Map<String, Object> forestData = new HashMap<>();
        forestData.put("playerCode", playerCode);
        forestData.put("region", region);
        
        try {
            if (response == null || response.trim().isEmpty()) {
                log.warn("산림청 API 응답이 비어있음");
                return createDefaultForestData(playerCode, region);
            }
            
            // Jackson ObjectMapper를 사용한 실제 JSON 파싱
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response);
            
            // 산림청 API 응답 구조: response.body.items[]
            com.fasterxml.jackson.databind.JsonNode responseNode = root.path("response");
            com.fasterxml.jackson.databind.JsonNode bodyNode = responseNode.path("body");
            
            // 에러 코드 확인
            com.fasterxml.jackson.databind.JsonNode headerNode = responseNode.path("header");
            String resultCode = headerNode.path("resultCode").asText();
            if (!"00".equals(resultCode)) {
                log.error("산림청 API 오류: {} - {}", resultCode, headerNode.path("resultMsg").asText());
                return createDefaultForestData(playerCode, region);
            }
            
            com.fasterxml.jackson.databind.JsonNode itemsNode = bodyNode.path("items");
            
            if (itemsNode.isArray() && itemsNode.size() > 0) {
                // 첫 번째 산불 예보 데이터 사용
                com.fasterxml.jackson.databind.JsonNode item = itemsNode.get(0);
                
                // 산불 위험도 정보 추출
                String dangerLevel = item.path("dangerLevel").asText("보통");
                String dangerCode = item.path("dangerCode").asText("2");
                String weatherCondition = item.path("weatherCondition").asText("일반");
                String humidity = item.path("humidity").asText("-");
                String windSpeed = item.path("windSpeed").asText("-");
                String temperature = item.path("temperature").asText("-");
                
                forestData.put("dangerLevel", getDangerLevelKorean(dangerCode));
                forestData.put("dangerCode", dangerCode);
                forestData.put("weatherCondition", weatherCondition);
                forestData.put("humidity", humidity);
                forestData.put("windSpeed", windSpeed);
                forestData.put("temperature", temperature);
                forestData.put("advice", getFireAdvice(dangerCode));
                
                log.debug("산불 데이터 파싱 완료: 위험도={}", forestData.get("dangerLevel"));
                
            } else {
                log.warn("산림청 API 응답에 데이터 없음");
                return createDefaultForestData(playerCode, region);
            }
            
        } catch (Exception e) {
            log.error("산불 데이터 파싱 실패: {}", e.getMessage());
            return createDefaultForestData(playerCode, region);
        }
        
        return forestData;
    }
    
    private String getDangerLevelKorean(String dangerCode) {
        switch (dangerCode) {
            case "1": return "낮음";
            case "2": return "보통";
            case "3": return "높음";
            case "4": return "매우높음";
            case "5": return "극도높음";
            default: return "보통";
        }
    }
    
    private String getFireAdvice(String dangerCode) {
        switch (dangerCode) {
            case "1": return "산불 발생 가능성이 낮습니다. 평상시 주의하세요.";
            case "2": return "산불 발생 가능성이 보통입니다. 기본적인 주의가 필요합니다.";
            case "3": return "산불 발생 가능성이 높습니다. 산행 시 각별한 주의가 필요합니다.";
            case "4": return "산불 발생 가능성이 매우 높습니다. 산행을 자제해주세요.";
            case "5": return "산불 발생 가능성이 극도로 높습니다. 산행을 금지합니다.";
            default: return "산불 예방에 항상 주의하세요.";
        }
    }
    
    private Map<String, Object> createDefaultForestData(String playerCode, String region) {
        Map<String, Object> defaultData = new HashMap<>();
        defaultData.put("playerCode", playerCode);
        defaultData.put("region", region);
        defaultData.put("dangerLevel", "보통");
        defaultData.put("dangerCode", "2");
        defaultData.put("weatherCondition", "일반");
        defaultData.put("humidity", "60%");
        defaultData.put("windSpeed", "2m/s");
        defaultData.put("temperature", "20℃");
        defaultData.put("advice", "산불 예방에 항상 주의하세요.");
        return defaultData;
    }
}
