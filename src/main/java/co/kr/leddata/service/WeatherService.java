package co.kr.leddata.service;

import co.kr.leddata.entity.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class WeatherService {
    
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    
    @Value("${api.weather.service-key}")
    private String serviceKey;
    
    @Value("${api.weather.base-url}")
    private String baseUrl;
    
    private final RestTemplate restTemplate;
    
    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public WeatherData getWeatherData(String playerCode, int nx, int ny, String region) {
        try {
            String url = buildWeatherUrl(nx, ny);
            log.debug("기상청 API 호출: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            log.debug("기상청 API 응답 받음");
            
            // JSON 응답 파싱 후 WeatherData 객체 생성
            WeatherData weatherData = parseWeatherResponse(response, playerCode, region);
            
            log.info("날씨 데이터 조회 성공: {} - {}°C", playerCode, weatherData.getTemperature());
            return weatherData;
            
        } catch (Exception e) {
            log.error("날씨 데이터 조회 실패: {} - {}", playerCode, e.getMessage());
            return createDefaultWeatherData(playerCode, region);
        }
    }
    
    private String buildWeatherUrl(int nx, int ny) {
        LocalDateTime now = LocalDateTime.now();
        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = getBaseTime(now.getHour());
        
        return UriComponentsBuilder.fromHttpUrl(baseUrl + "/getVilageFcst")
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", "1")
                .queryParam("numOfRows", "1000")
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .toUriString();
    }
    
    private String getBaseTime(int hour) {
        // 기상청 API 발표 시간에 맞춰 조정
        if (hour < 2) return "2300";
        else if (hour < 5) return "0200"; 
        else if (hour < 8) return "0500";
        else if (hour < 11) return "0800";
        else if (hour < 14) return "1100";
        else if (hour < 17) return "1400";
        else if (hour < 20) return "1700";
        else if (hour < 23) return "2000";
        else return "2300";
    }
    
    private WeatherData parseWeatherResponse(String response, String playerCode, String region) {
        WeatherData weatherData = new WeatherData(playerCode, region);
        
        try {
            if (response == null || response.trim().isEmpty()) {
                log.warn("API 응답이 비어있음");
                return createDefaultWeatherData(playerCode, region);
            }
            
            // Jackson ObjectMapper를 사용한 실제 JSON 파싱
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response);
            
            // 기상청 API 응답 구조: response.body.items.item[]
            com.fasterxml.jackson.databind.JsonNode responseNode = root.path("response");
            com.fasterxml.jackson.databind.JsonNode bodyNode = responseNode.path("body");
            
            // 에러 코드 확인
            com.fasterxml.jackson.databind.JsonNode headerNode = responseNode.path("header");
            String resultCode = headerNode.path("resultCode").asText();
            if (!"00".equals(resultCode)) {
                log.error("기상청 API 오류: {} - {}", resultCode, headerNode.path("resultMsg").asText());
                return createDefaultWeatherData(playerCode, region);
            }
            
            com.fasterxml.jackson.databind.JsonNode itemsNode = bodyNode.path("items").path("item");
            
            if (itemsNode.isArray() && itemsNode.size() > 0) {
                // 예보 데이터를 카테고리별로 파싱
                Double temperature = null;
                Integer humidity = null;
                Integer rainProbability = null;
                Double windSpeed = null;
                String skyCode = null;
                String precipitationType = null;
                
                for (com.fasterxml.jackson.databind.JsonNode item : itemsNode) {
                    String category = item.path("category").asText();
                    String fcstValue = item.path("fcstValue").asText();
                    
                    switch (category) {
                        case "TMP": // 기온
                            temperature = Double.parseDouble(fcstValue);
                            break;
                        case "REH": // 습도
                            humidity = Integer.parseInt(fcstValue);
                            break;
                        case "POP": // 강수확률
                            rainProbability = Integer.parseInt(fcstValue);
                            break;
                        case "WSD": // 풍속
                            windSpeed = Double.parseDouble(fcstValue);
                            break;
                        case "SKY": // 하늘상태
                            skyCode = fcstValue;
                            break;
                        case "PTY": // 강수형태
                            precipitationType = fcstValue;
                            break;
                    }
                }
                
                // WeatherData 객체에 설정
                weatherData.setTemperature(temperature != null ? temperature : 20.0);
                weatherData.setHumidity(humidity != null ? humidity : 60);
                weatherData.setRainProbability(rainProbability != null ? rainProbability : 10);
                weatherData.setWindSpeed(windSpeed != null ? windSpeed : 2.5);
                
                // 하늘상태 및 날씨 해석
                weatherData.setSkyState(getSkyState(skyCode));
                weatherData.setWeather(getWeatherDescription(skyCode, precipitationType));
                weatherData.setWindDirection("남동풍"); // 풍향은 별도 API 필요
                weatherData.setSunrise("06:30");
                weatherData.setSunset("18:30");
                
                log.debug("날씨 데이터 파싱 완료: {}°C, {}", temperature, weatherData.getWeather());
                
            } else {
                log.warn("기상청 API 응답에 데이터 없음");
                return createDefaultWeatherData(playerCode, region);
            }
            
        } catch (Exception e) {
            log.error("날씨 데이터 파싱 실패: {}", e.getMessage());
            return createDefaultWeatherData(playerCode, region);
        }
        
        return weatherData;
    }
    
    private String getSkyState(String skyCode) {
        if (skyCode == null) return "정보없음";
        switch (skyCode) {
            case "1": return "맑음";
            case "3": return "구름많음";
            case "4": return "흐림";
            default: return "정보없음";
        }
    }
    
    private String getWeatherDescription(String skyCode, String precipitationType) {
        if (precipitationType != null && !"0".equals(precipitationType)) {
            switch (precipitationType) {
                case "1": return "비";
                case "2": return "비/눈";
                case "3": return "눈";
                case "4": return "소나기";
            }
        }
        return getSkyState(skyCode);
    }
    
    private WeatherData createDefaultWeatherData(String playerCode, String region) {
        WeatherData weather = new WeatherData(playerCode, region);
        weather.setTemperature(20.0);
        weather.setWeather("정보 없음");
        weather.setSkyState("정보 없음");
        weather.setHumidity(60);
        weather.setWindDirection("북풍");
        weather.setWindSpeed(2.5);
        weather.setRainProbability(10);
        weather.setSunrise("06:30");
        weather.setSunset("18:30");
        return weather;
    }
    
    private String getRandomWeather() {
        String[] weathers = {"맑음", "구름많음", "흐림", "소나기", "비", "눈"};
        return weathers[(int)(Math.random() * weathers.length)];
    }
    
    private String getRandomWindDirection() {
        String[] directions = {"북풍", "남풍", "동풍", "서풍", "북동풍", "북서풍", "남동풍", "남서풍"};
        return directions[(int)(Math.random() * directions.length)];
    }
}