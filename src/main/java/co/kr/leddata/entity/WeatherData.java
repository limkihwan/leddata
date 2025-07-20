package co.kr.leddata.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_data")
public class WeatherData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String playerCode;  // 중복 방지
    
    private String region;
    private Double temperature;
    private String weather;
    private String skyState;
    private Integer humidity;
    private String windDirection;
    private Double windSpeed;
    private Integer rainProbability;
    private String sunrise;
    private String sunset;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 기본 생성자
    public WeatherData() {
        this.updateTime = LocalDateTime.now();
    }
    
    // 생성자
    public WeatherData(String playerCode, String region) {
        this();
        this.playerCode = playerCode;
        this.region = region;
    }
    
    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPlayerCode() { return playerCode; }
    public void setPlayerCode(String playerCode) { this.playerCode = playerCode; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }
    
    public String getSkyState() { return skyState; }
    public void setSkyState(String skyState) { this.skyState = skyState; }
    
    public Integer getHumidity() { return humidity; }
    public void setHumidity(Integer humidity) { this.humidity = humidity; }
    
    public String getWindDirection() { return windDirection; }
    public void setWindDirection(String windDirection) { this.windDirection = windDirection; }
    
    public Double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }
    
    public Integer getRainProbability() { return rainProbability; }
    public void setRainProbability(Integer rainProbability) { this.rainProbability = rainProbability; }
    
    public String getSunrise() { return sunrise; }
    public void setSunrise(String sunrise) { this.sunrise = sunrise; }
    
    public String getSunset() { return sunset; }
    public void setSunset(String sunset) { this.sunset = sunset; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    @Override
    public String toString() {
        return "WeatherData{" +
                "playerCode='" + playerCode + '\'' +
                ", region='" + region + '\'' +
                ", temperature=" + temperature +
                ", weather='" + weather + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}