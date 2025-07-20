package co.kr.leddata.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "air_station")
public class AirStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "station_name", unique = true)
    private String stationName;
    
    @Column(name = "station_sigu")
    private String stationSigu;
    
    @Column(name = "station_code", unique = true)
    private String stationCode;
    
    private String addr;
    
    @Column(name = "dm_x")
    private String dmX; // 경도
    
    @Column(name = "dm_y")
    private String dmY; // 위도
    
    // 생성자
    public AirStation() {}
    
    public AirStation(String stationName, String stationSigu, String stationCode) {
        this.stationName = stationName;
        this.stationSigu = stationSigu;
        this.stationCode = stationCode;
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
    
    @Override
    public String toString() {
        return String.format("AirStation{name='%s', sigu='%s', code='%s'}", 
                           stationName, stationSigu, stationCode);
    }
}