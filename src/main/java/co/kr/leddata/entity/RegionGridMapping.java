package co.kr.leddata.entity;

import jakarta.persistence.*;

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
    
    private Double lonTotal; // 경도
    private Double latTotal; // 위도
    
    // 생성자
    public RegionGridMapping() {}
    
    public RegionGridMapping(String code, String name1, String name2, String name3, Integer nx, Integer ny) {
        this.code = code;
        this.name1 = name1;
        this.name2 = name2;
        this.name3 = name3;
        this.nx = nx;
        this.ny = ny;
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
    
    public Double getLonTotal() { return lonTotal; }
    public void setLonTotal(Double lonTotal) { this.lonTotal = lonTotal; }
    
    public Double getLatTotal() { return latTotal; }
    public void setLatTotal(Double latTotal) { this.latTotal = latTotal; }
    
    // 전체 주소 조합
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (name1 != null && !name1.equals("nan")) sb.append(name1).append(" ");
        if (name2 != null && !name2.equals("nan")) sb.append(name2).append(" ");
        if (name3 != null && !name3.equals("nan")) sb.append(name3);
        return sb.toString().trim();
    }
    
    @Override
    public String toString() {
        return String.format("RegionGridMapping{code='%s', address='%s', nx=%d, ny=%d}", 
                           code, getFullAddress(), nx, ny);
    }
}