package co.kr.leddata.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "air_station")
public class AirStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "station_name")
    private String stationName;
    
    @Column(name = "station_sigu")
    private String stationSigu;
    
    @Column(name = "station_code", unique = true)
    private String stationCode;
    
    private String addr;
    
    // 좌표 정보
    @Column(name = "dm_x")
    private String dmX; // TM X 좌표
    
    @Column(name = "dm_y")
    private String dmY; // TM Y 좌표
    
    // WGS84 좌표 (변환된 값)
    private String longitude; // 경도
    private String latitude;  // 위도
    
    // 타임스탬프 필드
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 생성자
    public AirStation() {}
    
    public AirStation(String stationName, String stationSigu, String stationCode) {
        this.stationName = stationName;
        this.stationSigu = stationSigu;
        this.stationCode = stationCode;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // JPA 라이프사이클 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    
    public String getStationSigu() { return stationSigu; }
    public void setStationSigu(String stationSigu) { this.stationSigu = stationSigu; }
    
    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }
    
    public String getAddr() { return addr; }
    public void setAddr(String addr) { this.addr = addr; }
    
    public String getDmX() { return dmX; }
    public void setDmX(String dmX) { this.dmX = dmX; }
    
    public String getDmY() { return dmY; }
    public void setDmY(String dmY) { this.dmY = dmY; }
    
    // WGS84 좌표
    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }
    
    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }
    
    // 타임스탬프
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // 비즈니스 메서드
    public String getFullStationName() {
        if (stationSigu != null && stationName != null) {
            return stationSigu + " " + stationName;
        }
        return stationName != null ? stationName : "측정소명 없음";
    }
    
    // 좌표 유효성 검사
    public boolean hasValidCoordinates() {
        return longitude != null && latitude != null && 
               !longitude.trim().isEmpty() && !latitude.trim().isEmpty();
    }
    
    // TM 좌표 유효성 검사
    public boolean hasValidTmCoordinates() {
        return dmX != null && dmY != null && 
               !dmX.trim().isEmpty() && !dmY.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("AirStation{name='%s', sigu='%s', code='%s', addr='%s'}", 
                           stationName, stationSigu, stationCode, addr);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AirStation that = (AirStation) obj;
        return stationCode != null ? stationCode.equals(that.stationCode) : that.stationCode == null;
    }
    
    @Override
    public int hashCode() {
        return stationCode != null ? stationCode.hashCode() : 0;
    }
}