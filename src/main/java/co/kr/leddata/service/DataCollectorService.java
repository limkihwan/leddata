package co.kr.leddata.service;

import co.kr.leddata.entity.WeatherData;
import co.kr.leddata.entity.AirData;
import co.kr.leddata.repository.WeatherRepository;
import co.kr.leddata.repository.AirRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class DataCollectorService {
    
    private static final Logger log = LoggerFactory.getLogger(DataCollectorService.class);
    
    @Autowired
    private WeatherService weatherService;
    
    @Autowired
    private AirService airService;
    
    @Autowired
    private WeatherRepository weatherRepository;
    
    @Autowired
    private AirRepository airRepository;
    
    @Autowired
    private ForestService forestService;
    
    @Autowired
    private PlayerService playerService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Scheduled(cron = "${scheduler.weather.cron}")
    public void collectWeatherData() {
        log.info("=== 날씨 데이터 수집 시작 ===");
        
        try {
            Map<String, Object> playerConfigs = loadPlayerConfigs();
            int successCount = 0;
            int totalCount = playerConfigs.size();
            
            for (Map.Entry<String, Object> entry : playerConfigs.entrySet()) {
                String playerCode = entry.getKey();
                
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> config = (Map<String, Object>) entry.getValue();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> location = (Map<String, Object>) config.get("location");
                    
                    int nx = ((Number) location.get("nx")).intValue();
                    int ny = ((Number) location.get("ny")).intValue();
                    String region = (String) location.get("region");
                    
                    // 기존 데이터 삭제 (중복 방지)
                    weatherRepository.deleteByPlayerCode(playerCode);
                    
                    // 새 데이터 수집
                    WeatherData weatherData = weatherService.getWeatherData(playerCode, nx, ny, region);
                    if (weatherData != null) {
                        weatherRepository.save(weatherData);
                        successCount++;
                        log.debug("날씨 데이터 저장 성공: {} - {}°C", playerCode, weatherData.getTemperature());
                    }
                    
                } catch (Exception e) {
                    log.error("플레이어 {} 날씨 데이터 수집 실패: {}", playerCode, e.getMessage());
                }
            }
            
            log.info("날씨 데이터 수집 완료: {}/{} 성공", successCount, totalCount);
            
        } catch (Exception e) {
            log.error("날씨 데이터 수집 중 오류: {}", e.getMessage());
        }
    }
    
    @Scheduled(cron = "${scheduler.air.cron}")
    public void collectAirData() {
        log.info("=== 대기질 데이터 수집 시작 ===");
        
        try {
            Map<String, Object> playerConfigs = loadPlayerConfigs();
            int successCount = 0;
            int totalCount = playerConfigs.size();
            
            for (Map.Entry<String, Object> entry : playerConfigs.entrySet()) {
                String playerCode = entry.getKey();
                
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> config = (Map<String, Object>) entry.getValue();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> location = (Map<String, Object>) config.get("location");
                    
                    String stationName = (String) location.get("stationName");
                    String region = (String) location.get("region");
                    
                    // 기존 데이터 삭제 (중복 방지)
                    airRepository.deleteByPlayerCode(playerCode);
                    
                    // 새 데이터 수집
                    AirData airData = airService.getAirData(playerCode, stationName, region);
                    if (airData != null) {
                        airRepository.save(airData);
                        successCount++;
                        log.debug("대기질 데이터 저장 성공: {} - PM10: {}μg/m³", playerCode, airData.getPm10Value());
                    }
                    
                } catch (Exception e) {
                    log.error("플레이어 {} 대기질 데이터 수집 실패: {}", playerCode, e.getMessage());
                }
            }
            
            log.info("대기질 데이터 수집 완료: {}/{} 성공", successCount, totalCount);
            
        } catch (Exception e) {
            log.error("대기질 데이터 수집 중 오류: {}", e.getMessage());
        }
    }
    
    @Scheduled(cron = "${scheduler.forest.cron}")
    public void collectForestData() {
        log.info("=== 산불 데이터 수집 시작 ===");
        
        try {
            Map<String, Object> playerConfigs = loadPlayerConfigs();
            int successCount = 0;
            int totalCount = playerConfigs.size();
            
            for (Map.Entry<String, Object> entry : playerConfigs.entrySet()) {
                String playerCode = entry.getKey();
                
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> config = (Map<String, Object>) entry.getValue();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> location = (Map<String, Object>) config.get("location");
                    
                    String region = (String) location.get("region");
                    // 지역코드는 player config에서 추가로 관리하거나 지역명에서 추출
                    String regionCode = getRegionCodeFromName(region);
                    
                    // 산불 데이터 수집 (DB 저장하지 않고 로그만)
                    Map<String, Object> forestData = forestService.getForestFireData(playerCode, regionCode, region);
                    if (forestData != null) {
                        successCount++;
                        log.debug("산불 데이터 수집 성공: {} - 위험도: {}", playerCode, forestData.get("dangerLevel"));
                    }
                    
                } catch (Exception e) {
                    log.error("플레이어 {} 산불 데이터 수집 실패: {}", playerCode, e.getMessage());
                }
            }
            
            log.info("산불 데이터 수집 완료: {}/{} 성공", successCount, totalCount);
            
        } catch (Exception e) {
            log.error("산불 데이터 수집 중 오류: {}", e.getMessage());
        }
    }
    
    // 수동 데이터 수집 메서드 (테스트용)
    public void collectAllDataNow() {
        log.info("=== 수동 데이터 수집 시작 ===");
        collectWeatherData();
        collectAirData();
        collectForestData();
        log.info("=== 수동 데이터 수집 완료 ===");
    }
    
    private String getRegionCodeFromName(String regionName) {
        // 지역명에서 시도코드 추출 (간단한 매핑)
        if (regionName == null) return "11";
        
        if (regionName.contains("서울")) return "11";
        else if (regionName.contains("부산")) return "26";
        else if (regionName.contains("대구")) return "27";
        else if (regionName.contains("인천")) return "28";
        else if (regionName.contains("광주")) return "29";
        else if (regionName.contains("대전")) return "30";
        else if (regionName.contains("울산")) return "31";
        else if (regionName.contains("세종")) return "36";
        else if (regionName.contains("경기")) return "41";
        else if (regionName.contains("강원")) return "42";
        else if (regionName.contains("충북") || regionName.contains("충청북도")) return "43";
        else if (regionName.contains("충남") || regionName.contains("충청남도")) return "44";
        else if (regionName.contains("전북") || regionName.contains("전라북도")) return "45";
        else if (regionName.contains("전남") || regionName.contains("전라남도")) return "46";
        else if (regionName.contains("경북") || regionName.contains("경상북도")) return "47";
        else if (regionName.contains("경남") || regionName.contains("경상남도")) return "48";
        else if (regionName.contains("제주")) return "50";
        else return "11"; // 기본값: 서울
    }
    
    // 비활성 세션 정리 (매 시간마다)
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupInactiveSessions() {
        log.info("=== 비활성 세션 정리 시작 ===");
        try {
            playerService.cleanupInactiveSessions();
        } catch (Exception e) {
            log.error("비활성 세션 정리 중 오류: {}", e.getMessage());
        }
        log.info("=== 비활성 세션 정리 완료 ===");
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadPlayerConfigs() throws IOException {
        ClassPathResource resource = new ClassPathResource("player-config.json");
        JsonNode rootNode = objectMapper.readTree(resource.getInputStream());
        return objectMapper.convertValue(rootNode, Map.class);
    }
}