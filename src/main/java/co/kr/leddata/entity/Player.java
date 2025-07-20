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
    
    // 해상도 (가로x세로 분리)
    @Column(name = "resolution_width")
    private Integer resolutionWidth;
    
    @Column(name = "resolution_height")
    private Integer resolutionHeight;
    
    // 서비스 기간
    @Column(name = "service_start_date")
    private LocalDateTime serviceStartDate;
    
    @Column(name = "service_end_date")
    private LocalDateTime serviceEndDate;
    
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
    
    public Integer getResolutionWidth() { return resolutionWidth; }
    public void setResolutionWidth(Integer resolutionWidth) { this.resolutionWidth = resolutionWidth; }
    
    public Integer getResolutionHeight() { return resolutionHeight; }
    public void setResolutionHeight(Integer resolutionHeight) { this.resolutionHeight = resolutionHeight; }
    
    public LocalDateTime getServiceStartDate() { return serviceStartDate; }
    public void setServiceStartDate(LocalDateTime serviceStartDate) { this.serviceStartDate = serviceStartDate; }
    
    public LocalDateTime getServiceEndDate() { return serviceEndDate; }
    public void setServiceEndDate(LocalDateTime serviceEndDate) { this.serviceEndDate = serviceEndDate; }
    
    // 서비스 기간 체크 메서드
    public boolean isServiceActive() {
        LocalDateTime now = LocalDateTime.now();
        if (serviceStartDate != null && now.isBefore(serviceStartDate)) {
            return false; // 서비스 시작 전
        }
        if (serviceEndDate != null && now.isAfter(serviceEndDate)) {
            return false; // 서비스 종료 후
        }
        return true;
    }
    
    // 접속 상태 판단 메서드
    public String getActualConnectionStatus() {
        if (!accessAllowed || !isServiceActive()) {
            return "BLOCKED"; // 접속차단
        }
        return connectionStatus != null ? connectionStatus : "DISCONNECTED";
    }
    
    // 해상도 문자열 반환 (호환성을 위해)
    public String getResolution() {
        if (resolutionWidth != null && resolutionHeight != null) {
            return resolutionWidth + "x" + resolutionHeight;
        }
        return "";
    }
    
    // 해상도 문자열 설정 (호환성을 위해)
    public void setResolution(String resolution) {
        if (resolution != null && resolution.contains("x")) {
            String[] parts = resolution.split("x");
            if (parts.length == 2) {
                try {
                    this.resolutionWidth = Integer.parseInt(parts[0]);
                    this.resolutionHeight = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    // 파싱 실패 시 무시
                }
            }
        }
    }
    
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
