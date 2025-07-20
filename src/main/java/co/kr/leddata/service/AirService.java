package co.kr.leddata.service;

import co.kr.leddata.entity.AirData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AirService {
    
    private static final Logger log = LoggerFactory.getLogger(AirService.class);
    
    @Value("${api.air.service-key}")
    private String serviceKey;
    
    @Value("${api.air.base-url}")
    private String baseUrl;
    
    private final RestTemplate restTemplate;
    
    public AirService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public AirData getAirData(String playerCode, String stationName, String region) {
        try {
            String url = buildAirUrl(stationName);
            log.debug("대기질 API 호출: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            log.debug("대기질 API 응답 받음");
            
            // JSON 응답 파싱 후 AirData 객체 생성
            AirData airData = parseAirResponse(response, playerCode, stationName, region);
            
            log.info("대기질 데이터 조회 성공: {} - PM10: {}μg/m³", playerCode, airData.getPm10Value());
            return airData;
            
        } catch (Exception e) {
            log.error("대기질 데이터 조회 실패: {} - {}", playerCode, e.getMessage());
            return createDefaultAirData(playerCode, stationName, region);
        }
    }
    
    private String buildAirUrl(String stationName) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl + "/getMsrstnAcctoRltmMesureDnsty")
                .queryParam("serviceKey", serviceKey)
                .queryParam("returnType", "json")
                .queryParam("numOfRows", "1")
                .queryParam("pageNo", "1")
                .queryParam("stationName", stationName)
                .queryParam("dataTerm", "DAILY")
                .queryParam("ver", "1.0")
                .toUriString();
    }
    
    private AirData parseAirResponse(String response, String playerCode, String stationName, String region) {
        AirData airData = new AirData(playerCode, stationName, region);
        
        try {
            if (response == null || response.trim().isEmpty()) {
                log.warn("대기질 API 응답이 비어있음");
                return createDefaultAirData(playerCode, stationName, region);
            }
            
            // Jackson ObjectMapper를 사용한 실제 JSON 파싱
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response);
            
            // 한국환경공단 API 응답 구조: response.body.items[]
            com.fasterxml.jackson.databind.JsonNode responseNode = root.path("response");
            com.fasterxml.jackson.databind.JsonNode bodyNode = responseNode.path("body");
            
            // 에러 코드 확인
            com.fasterxml.jackson.databind.JsonNode headerNode = responseNode.path("header");
            String resultCode = headerNode.path("resultCode").asText();
            if (!"00".equals(resultCode)) {
                log.error("대기질 API 오류: {} - {}", resultCode, headerNode.path("resultMsg").asText());
                return createDefaultAirData(playerCode, stationName, region);
            }
            
            com.fasterxml.jackson.databind.JsonNode itemsNode = bodyNode.path("items");
            
            if (itemsNode.isArray() && itemsNode.size() > 0) {
                // 첫 번째 측정소 데이터 사용
                com.fasterxml.jackson.databind.JsonNode item = itemsNode.get(0);
                
                // 미세먼지 PM10
                String pm10ValueStr = item.path("pm10Value").asText("-");
                if (!"-".equals(pm10ValueStr) && !pm10ValueStr.isEmpty()) {
                    try {
                        int pm10Value = Integer.parseInt(pm10ValueStr);
                        airData.setPm10Value(pm10Value);
                        airData.setPm10Grade(getPm10Grade(pm10Value));
                    } catch (NumberFormatException e) {
                        log.warn("PM10 값 파싱 실패: {}", pm10ValueStr);
                    }
                }
                
                // 초미세먼지 PM2.5
                String pm25ValueStr = item.path("pm25Value").asText("-");
                if (!"-".equals(pm25ValueStr) && !pm25ValueStr.isEmpty()) {
                    try {
                        int pm25Value = Integer.parseInt(pm25ValueStr);
                        airData.setPm25Value(pm25Value);
                        airData.setPm25Grade(getPm25Grade(pm25Value));
                    } catch (NumberFormatException e) {
                        log.warn("PM2.5 값 파싱 실패: {}", pm25ValueStr);
                    }
                }
                
                // 통합대기환경지수 (CAI)
                String khaiValueStr = item.path("khaiValue").asText("-");
                if (!"-".equals(khaiValueStr) && !khaiValueStr.isEmpty()) {
                    try {
                        int caiValue = Integer.parseInt(khaiValueStr);
                        airData.setCaiValue(caiValue);
                        airData.setCaiGrade(getCaiGrade(caiValue));
                    } catch (NumberFormatException e) {
                        log.warn("CAI 값 파싱 실패: {}", khaiValueStr);
                    }
                }
                
                // 오존 (O3)
                String o3ValueStr = item.path("o3Value").asText("-");
                if (!"-".equals(o3ValueStr) && !o3ValueStr.isEmpty()) {
                    try {
                        double o3Value = Double.parseDouble(o3ValueStr);
                        airData.setO3Value(o3Value);
                        airData.setO3Grade(getO3Grade(o3Value));
                    } catch (NumberFormatException e) {
                        log.warn("O3 값 파싱 실패: {}", o3ValueStr);
                    }
                }
                
                // 이산화질소 (NO2)
                String no2ValueStr = item.path("no2Value").asText("-");
                if (!"-".equals(no2ValueStr) && !no2ValueStr.isEmpty()) {
                    try {
                        double no2Value = Double.parseDouble(no2ValueStr);
                        airData.setNo2Value(no2Value);
                        airData.setNo2Grade(getNo2Grade(no2Value));
                    } catch (NumberFormatException e) {
                        log.warn("NO2 값 파싱 실패: {}", no2ValueStr);
                    }
                }
                
                // 일산화탄소 (CO)
                String coValueStr = item.path("coValue").asText("-");
                if (!"-".equals(coValueStr) && !coValueStr.isEmpty()) {
                    try {
                        double coValue = Double.parseDouble(coValueStr);
                        airData.setCoValue(coValue);
                        airData.setCoGrade(getCoGrade(coValue));
                    } catch (NumberFormatException e) {
                        log.warn("CO 값 파싱 실패: {}", coValueStr);
                    }
                }
                
                // 아황산가스 (SO2)
                String so2ValueStr = item.path("so2Value").asText("-");
                if (!"-".equals(so2ValueStr) && !so2ValueStr.isEmpty()) {
                    try {
                        double so2Value = Double.parseDouble(so2ValueStr);
                        airData.setSo2Value(so2Value);
                        airData.setSo2Grade(getSo2Grade(so2Value));
                    } catch (NumberFormatException e) {
                        log.warn("SO2 값 파싱 실패: {}", so2ValueStr);
                    }
                }
                
                log.debug("대기질 데이터 파싱 완료: PM10={}, PM2.5={}", 
                         airData.getPm10Value(), airData.getPm25Value());
                
            } else {
                log.warn("대기질 API 응답에 데이터 없음");
                return createDefaultAirData(playerCode, stationName, region);
            }
            
        } catch (Exception e) {
            log.error("대기질 데이터 파싱 실패: {}", e.getMessage());
            return createDefaultAirData(playerCode, stationName, region);
        }
        
        return airData;
    }
    
    private String getO3Grade(double value) {
        if (value <= 0.030) return "좋음";
        else if (value <= 0.090) return "보통";
        else if (value <= 0.150) return "나쁨";
        else return "매우나쁨";
    }
    
    private String getNo2Grade(double value) {
        if (value <= 0.030) return "좋음";
        else if (value <= 0.060) return "보통";
        else if (value <= 0.200) return "나쁨";
        else return "매우나쁨";
    }
    
    private String getCoGrade(double value) {
        if (value <= 2.0) return "좋음";
        else if (value <= 9.0) return "보통";
        else if (value <= 15.0) return "나쁨";
        else return "매우나쁨";
    }
    
    private String getSo2Grade(double value) {
        if (value <= 0.020) return "좋음";
        else if (value <= 0.050) return "보통";
        else if (value <= 0.150) return "나쁨";
        else return "매우나쁨";
    }
    
    private AirData createDefaultAirData(String playerCode, String stationName, String region) {
        AirData air = new AirData(playerCode, stationName, region);
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
    
    private String getPm10Grade(int value) {
        if (value <= 30) return "좋음";
        else if (value <= 80) return "보통";
        else if (value <= 150) return "나쁨";
        else return "매우나쁨";
    }
    
    private String getPm25Grade(int value) {
        if (value <= 15) return "좋음";
        else if (value <= 35) return "보통";
        else if (value <= 75) return "나쁨";
        else return "매우나쁨";
    }
    
    private String getCaiGrade(int value) {
        if (value <= 50) return "좋음";
        else if (value <= 100) return "보통";
        else if (value <= 250) return "나쁨";
        else return "매우나쁨";
    }
}