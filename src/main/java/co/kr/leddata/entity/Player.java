package co.kr.leddata.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "players")
public class Player {
    
    @Id
    @Column(name = "player_code", length = 6)
    private String playerCode;
    
    @Column(name = "player_name", length = 100)
    private String playerName;
    
    // 대기정보 위치값
    @Column(name = "air_station_name", length = 50)
    private String airStationName;
    
    // 산불정보 위치값 (시도코드)
    @Column(name = "forest_region_code", length = 2)
    private String forestRegionCode;
    
    // 날씨정보 위치값 (격자 좌표)
    @Column(name = "weather_nx")
    private Integer weatherNx;
    
    @Column(name = "weather_ny")
    private Integer weatherNy;
    
    // 지역명
    @Column(name = "region", length = 100)
    private String region;
    
    // 테마
    @Column(name = "theme", length = 50)
    private String theme;
    
    // 해상도
    @Column(name = "resolution", length = 20)
    private String resolution;
    
    // 접속 상태
    @Column(name = "connection_status")
    private String connectionStatus = "DISCONNECTED"; // CONNECTED, DISCONNECTED, WAITING
    
    // 접속 허가 상태
    @Column(name = "access_allowed")
    private Boolean accessAllowed = true;
    
    // 현재 접속 세션 ID
    @Column(name = "current_session_id")
    private String currentSessionId;
    
    // 마지막 접속 시간
    @Column(name = "last_connected_at")
    private LocalDateTime lastConnectedAt;
    
    // 생성/수정 시간
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 기본 생성자
    public Player() {}
    
    // 생성자
    public Player(String playerCode, String playerName) {
        this.playerCode = playerCode;
        this.playerName = playerName;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getPlayerCode() { return playerCode; }
    public void setPlayerCode(String playerCode) { this.playerCode = playerCode; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public String getAirStationName() { return airStationName; }
    public void setAirStationName(String airStationName) { this.airStationName = airStationName; }
    
    public String getForestRegionCode() { return forestRegionCode; }
    public void setForestRegionCode(String forestRegionCode) { this.forestRegionCode = forestRegionCode; }
    
    public Integer getWeatherNx() { return weatherNx; }
    public void setWeatherNx(Integer weatherNx) { this.weatherNx = weatherNx; }
    
    public Integer getWeatherNy() { return weatherNy; }
    public void setWeatherNy(Integer weatherNy) { this.weatherNy = weatherNy; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    
    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { this.connectionStatus = connectionStatus; }
    
    public Boolean getAccessAllowed() { return accessAllowed; }
    public void setAccessAllowed(Boolean accessAllowed) { this.accessAllowed = accessAllowed; }
    
    public String getCurrentSessionId() { return currentSessionId; }
    public void setCurrentSessionId(String currentSessionId) { this.currentSessionId = currentSessionId; }
    
    public LocalDateTime getLastConnectedAt() { return lastConnectedAt; }
    public void setLastConnectedAt(LocalDateTime lastConnectedAt) { this.lastConnectedAt = lastConnectedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
