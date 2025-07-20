package co.kr.leddata.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "region_grid_mapping")
public class RegionGridMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String code;
    
    private String name1; // 시/도
    private String name2; // 시/군/구
    private String name3; // 동/읍/면
    
    private Integer nx;   // 기상청 격자 X
    private Integer ny;   // 기상청 격자 Y
    
    // 경위도 필드 추가
    private Double longitude; // 경도
    private Double latitude;  // 위도
    
    // 기존 필드들 (호환성 유지)
    private Double lonTotal; // 경도 (legacy)
    private Double latTotal; // 위도 (legacy)
    
    // 타임스탬프 필드 추가
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 생성자
    public RegionGridMapping() {}
    
    public RegionGridMapping(String code, String name1, String name2, String name3, Integer nx, Integer ny) {
        this.code = code;
        this.name1 = name1;
        this.name2 = name2;
        this.name3 = name3;
        this.nx = nx;
        this.ny = ny;
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
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName1() { return name1; }
    public void setName1(String name1) { this.name1 = name1; }
    
    public String getName2() { return name2; }
    public void setName2(String name2) { this.name2 = name2; }
    
    public String getName3() { return name3; }
    public void setName3(String name3) { this.name3 = name3; }
    
    public Integer getNx() { return nx; }
    public void setNx(Integer nx) { this.nx = nx; }
    
    public Integer getNy() { return ny; }
    public void setNy(Integer ny) { this.ny = ny; }
    
    // 새로운 경위도 필드
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { 
        this.longitude = longitude;
        this.lonTotal = longitude; // 호환성 유지
    }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { 
        this.latitude = latitude;
        this.latTotal = latitude; // 호환성 유지
    }
    
    // 기존 필드들 (호환성 유지)
    public Double getLonTotal() { return lonTotal; }
    public void setLonTotal(Double lonTotal) { 
        this.lonTotal = lonTotal;
        if (this.longitude == null) this.longitude = lonTotal;
    }
    
    public Double getLatTotal() { return latTotal; }
    public void setLatTotal(Double latTotal) { 
        this.latTotal = latTotal;
        if (this.latitude == null) this.latitude = latTotal;
    }
    
    // 타임스탬프 필드
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // 전체 주소 조합
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (name1 != null && !name1.equals("nan")) sb.append(name1).append(" ");
        if (name2 != null && !name2.equals("nan")) sb.append(name2).append(" ");
        if (name3 != null && !name3.equals("nan")) sb.append(name3);
        return sb.toString().trim();
    }
    
    // 지역명 반환 (검색용)
    public String getRegionName() {
        return getFullAddress();
    }
    
    // 좌표 유효성 검사
    public boolean hasValidCoordinates() {
        return nx != null && ny != null && nx > 0 && ny > 0;
    }
    
    // 경위도 유효성 검사
    public boolean hasValidLatLon() {
        return longitude != null && latitude != null && 
               longitude >= 124.0 && longitude <= 132.0 && 
               latitude >= 33.0 && latitude <= 38.0;
    }
    
    @Override
    public String toString() {
        return String.format("RegionGridMapping{code='%s', address='%s', nx=%d, ny=%d, lon=%.4f, lat=%.4f}", 
                           code, getFullAddress(), nx, ny, longitude, latitude);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RegionGridMapping that = (RegionGridMapping) obj;
        return code != null ? code.equals(that.code) : that.code == null;
    }
    
    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }
}