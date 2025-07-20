package co.kr.leddata.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "air_data")
public class AirData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String playerCode;  // 중복 방지
    
    private String stationName;
    private String region;
    
    // 미세먼지 (PM10)
    private Integer pm10Value;
    private String pm10Grade;
    
    // 초미세먼지 (PM2.5)
    private Integer pm25Value; 
    private String pm25Grade;
    
    // 통합대기환경지수 (CAI)
    private Integer caiValue;
    private String caiGrade;
    
    // 오존 (O3)
    private Double o3Value;
    private String o3Grade;
    
    // 이산화질소 (NO2)
    private Double no2Value;
    private String no2Grade;
    
    // 일산화탄소 (CO)
    private Double coValue;
    private String coGrade;
    
    // 아황산가스 (SO2)
    private Double so2Value;
    private String so2Grade;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 기본 생성자
    public AirData() {
        this.updateTime = LocalDateTime.now();
    }
    
    // 생성자
    public AirData(String playerCode, String stationName, String region) {
        this();
        this.playerCode = playerCode;
        this.stationName = stationName;
        this.region = region;
    }
    
    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPlayerCode() { return playerCode; }
    public void setPlayerCode(String playerCode) { this.playerCode = playerCode; }
    
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public Integer getPm10Value() { return pm10Value; }
    public void setPm10Value(Integer pm10Value) { this.pm10Value = pm10Value; }
    
    public String getPm10Grade() { return pm10Grade; }
    public void setPm10Grade(String pm10Grade) { this.pm10Grade = pm10Grade; }
    
    public Integer getPm25Value() { return pm25Value; }
    public void setPm25Value(Integer pm25Value) { this.pm25Value = pm25Value; }
    
    public String getPm25Grade() { return pm25Grade; }
    public void setPm25Grade(String pm25Grade) { this.pm25Grade = pm25Grade; }
    
    public Integer getCaiValue() { return caiValue; }
    public void setCaiValue(Integer caiValue) { this.caiValue = caiValue; }
    
    public String getCaiGrade() { return caiGrade; }
    public void setCaiGrade(String caiGrade) { this.caiGrade = caiGrade; }
    
    public Double getO3Value() { return o3Value; }
    public void setO3Value(Double o3Value) { this.o3Value = o3Value; }
    
    public String getO3Grade() { return o3Grade; }
    public void setO3Grade(String o3Grade) { this.o3Grade = o3Grade; }
    
    public Double getNo2Value() { return no2Value; }
    public void setNo2Value(Double no2Value) { this.no2Value = no2Value; }
    
    public String getNo2Grade() { return no2Grade; }
    public void setNo2Grade(String no2Grade) { this.no2Grade = no2Grade; }
    
    public Double getCoValue() { return coValue; }
    public void setCoValue(Double coValue) { this.coValue = coValue; }
    
    public String getCoGrade() { return coGrade; }
    public void setCoGrade(String coGrade) { this.coGrade = coGrade; }
    
    public Double getSo2Value() { return so2Value; }
    public void setSo2Value(Double so2Value) { this.so2Value = so2Value; }
    
    public String getSo2Grade() { return so2Grade; }
    public void setSo2Grade(String so2Grade) { this.so2Grade = so2Grade; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    @Override
    public String toString() {
        return "AirData{" +
                "playerCode='" + playerCode + '\'' +
                ", stationName='" + stationName + '\'' +
                ", region='" + region + '\'' +
                ", pm10Value=" + pm10Value +
                ", pm25Value=" + pm25Value +
                ", updateTime=" + updateTime +
                '}';
    }
}