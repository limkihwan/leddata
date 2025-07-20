package co.kr.leddata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/data")
public class DataInitController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @GetMapping("/init")
    @ResponseBody
    public Map<String, Object> initializeData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 기존 데이터 삭제 (안전하게)
            try {
                jdbcTemplate.execute("DELETE FROM REGION_GRID_MAPPING");
                jdbcTemplate.execute("DELETE FROM AIR_STATION");
            } catch (Exception e) {
                // 테이블이 없거나 비어있을 수 있음
            }
            
            // 지역 격자 데이터 초기화
            int regionCount = initRegionData();
            
            // 대기질 측정소 데이터 초기화
            int airStationCount = initAirStationData();
            
            result.put("success", true);
            result.put("regionCount", regionCount);
            result.put("airStationCount", airStationCount);
            result.put("message", "데이터 초기화가 완료되었습니다.");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    private int initRegionData() {
        String sql = """
            INSERT INTO REGION_GRID_MAPPING (code, name1, name2, name3, nx, ny) VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        // 주요 지역 격자 데이터
        Object[][] regions = {
            {"1168000000", "서울", "강남구", "", 61, 126},
            {"1174000000", "서울", "강동구", "", 62, 126},
            {"1130500000", "서울", "강북구", "", 61, 128},
            {"1150000000", "서울", "강서구", "", 58, 126},
            {"1162000000", "서울", "관악구", "", 59, 125},
            {"1121500000", "서울", "광진구", "", 62, 126},
            {"1153000000", "서울", "구로구", "", 58, 125},
            {"1154500000", "서울", "금천구", "", 59, 124},
            {"1135000000", "서울", "노원구", "", 61, 129},
            {"1132000000", "서울", "도봉구", "", 61, 129},
            {"1123000000", "서울", "동대문구", "", 61, 127},
            {"1159000000", "서울", "동작구", "", 59, 125},
            {"1144000000", "서울", "마포구", "", 59, 127},
            {"1141000000", "서울", "서대문구", "", 59, 127},
            {"1165000000", "서울", "서초구", "", 61, 125},
            {"1120000000", "서울", "성동구", "", 61, 127},
            {"1129000000", "서울", "성북구", "", 61, 127},
            {"1171000000", "서울", "송파구", "", 62, 125},
            {"1147000000", "서울", "양천구", "", 58, 126},
            {"1156000000", "서울", "영등포구", "", 58, 126},
            {"1117000000", "서울", "용산구", "", 60, 126},
            {"1138000000", "서울", "은평구", "", 59, 127},
            {"1111000000", "서울", "종로구", "", 60, 127},
            {"1114000000", "서울", "중구", "", 60, 127},
            {"1126000000", "서울", "중랑구", "", 62, 127},
            
            {"2611000000", "부산", "중구", "", 98, 76},
            {"2614000000", "부산", "서구", "", 97, 76},
            {"2617000000", "부산", "동구", "", 99, 76},
            {"2620000000", "부산", "영도구", "", 98, 75},
            {"2623000000", "부산", "부산진구", "", 98, 75}
        };
        
        int count = 0;
        for (Object[] region : regions) {
            jdbcTemplate.update(sql, region[0], region[1], region[2], region[3], region[4], region[5]);
            count++;
        }
        
        return count;
    }
    
    private int initAirStationData() {
        String sql = """
            INSERT INTO AIR_STATION (station_name, station_code, station_sigu, addr) 
            SELECT ?, ?, ?, ? 
            WHERE NOT EXISTS (SELECT 1 FROM AIR_STATION WHERE station_name = ? AND station_sigu = ?)
            """;
        
        // 주요 대기질 측정소 데이터
        Object[][] stations = {
            {"중구", "111123", "중구", "서울특별시 중구"},
            {"종로구", "111131", "종로구", "서울특별시 종로구"},
            {"용산구", "111142", "용산구", "서울특별시 용산구"},
            {"성동구", "111151", "성동구", "서울특별시 성동구"},
            {"광진구", "111161", "광진구", "서울특별시 광진구"},
            {"동대문구", "111171", "동대문구", "서울특별시 동대문구"},
            {"중랑구", "111181", "중랑구", "서울특별시 중랑구"},
            {"성북구", "111191", "성북구", "서울특별시 성북구"},
            {"강북구", "111201", "강북구", "서울특별시 강북구"},
            {"도봉구", "111211", "도봉구", "서울특별시 도봉구"},
            {"노원구", "111221", "노원구", "서울특별시 노원구"},
            {"은평구", "111231", "은평구", "서울특별시 은평구"},
            {"서대문구", "111241", "서대문구", "서울특별시 서대문구"},
            {"마포구", "111251", "마포구", "서울특별시 마포구"},
            {"양천구", "111261", "양천구", "서울특별시 양천구"},
            {"강서구", "111271", "강서구", "서울특별시 강서구"},
            {"구로구", "111281", "구로구", "서울특별시 구로구"},
            {"금천구", "111291", "금천구", "서울특별시 금천구"},
            {"영등포구", "111301", "영등포구", "서울특별시 영등포구"},
            {"동작구", "111311", "동작구", "서울특별시 동작구"},
            {"관악구", "111321", "관악구", "서울특별시 관악구"},
            {"서초구", "111331", "서초구", "서울특별시 서초구"},
            {"강남구", "111341", "강남구", "서울특별시 강남구"},
            {"송파구", "111351", "송파구", "서울특별시 송파구"},
            {"강동구", "111361", "강동구", "서울특별시 강동구"},
            
            {"중구", "261111", "중구", "부산광역시 중구"},
            {"서구", "261121", "서구", "부산광역시 서구"},
            {"동구", "261131", "동구", "부산광역시 동구"},
            {"영도구", "261141", "영도구", "부산광역시 영도구"},
            {"부산진구", "261151", "부산진구", "부산광역시 부산진구"}
        };
        
        int count = 0;
        for (Object[] station : stations) {
            int inserted = jdbcTemplate.update(sql, station[0], station[1], station[2], station[3], station[0], station[2]);
            if (inserted > 0) count++;
        }
        
        return count;
    }
    
    @GetMapping("/count")
    @ResponseBody
    public Map<String, Object> getDataCount() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int regionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM REGION_GRID_MAPPING", Integer.class);
            int airStationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM AIR_STATION", Integer.class);
            
            result.put("regionCount", regionCount);
            result.put("airStationCount", airStationCount);
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
