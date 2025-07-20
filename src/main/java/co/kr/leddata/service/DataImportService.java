package co.kr.leddata.service;

import co.kr.leddata.entity.AirStation;
import co.kr.leddata.entity.RegionGridMapping;
import co.kr.leddata.repository.AirStationRepository;
import co.kr.leddata.repository.RegionGridMappingRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DataImportService {
    
    private static final Logger log = LoggerFactory.getLogger(DataImportService.class);
    
    @Autowired
    private RegionGridMappingRepository regionRepository;
    
    @Autowired
    private AirStationRepository airStationRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${api.airkorea.service-key:}")
    private String airKoreaServiceKey;
    
    @Value("${api.airkorea.base-url:http://apis.data.go.kr/B552584}")
    private String airKoreaBaseUrl;
    
    /**
     * 엑셀 파일에서 행정구역 코드 데이터 가져오기
     */
    public Map<String, Object> importRegionFromExcel(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        Workbook workbook = null;
        try {
            // 파일 확장자에 따라 워크북 생성
            String fileName = file.getOriginalFilename();
            InputStream inputStream = file.getInputStream();
            
            if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
            }
            
            // 기존 데이터 삭제
            regionRepository.deleteAll();
            log.info("기존 행정구역 데이터 삭제 완료");
            
            int processedCount = 0;
            int errorCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            // 첫 번째 시트 읽기
            Sheet sheet = workbook.getSheetAt(0);
            
            // 헤더 행 분석 (첫 번째 행)
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnMap = analyzeHeaders(headerRow);
            
            // 데이터 행 처리 (두 번째 행부터)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                
                if (row == null) continue;
                
                try {
                    RegionGridMapping region = parseRegionFromRow(row, columnMap);
                    if (region != null) {
                        regionRepository.save(region);
                        processedCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                    errorMessages.add(String.format("행 %d: %s", i + 1, e.getMessage()));
                    log.warn("행 {} 처리 실패: {}", i + 1, e.getMessage());
                }
            }
            
            result.put("processedCount", processedCount);
            result.put("errorCount", errorCount);
            result.put("errorMessages", errorMessages);
            result.put("totalRows", sheet.getLastRowNum());
            
            log.info("엑셀 파일 처리 완료 - 성공: {}개, 실패: {}개", processedCount, errorCount);
            
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
        
        return result;
    }
    
    /**
     * 엑셀 헤더 분석
     */
    private Map<String, Integer> analyzeHeaders(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        if (headerRow != null) {
            for (Cell cell : headerRow) {
                String headerText = getCellValueAsString(cell).toLowerCase();
                
                // 헤더 매핑 (다양한 형태의 헤더 지원)
                if (headerText.contains("코드") || headerText.contains("code")) {
                    columnMap.put("code", cell.getColumnIndex());
                } else if (headerText.contains("시도") || headerText.contains("name1")) {
                    columnMap.put("name1", cell.getColumnIndex());
                } else if (headerText.contains("시군구") || headerText.contains("name2")) {
                    columnMap.put("name2", cell.getColumnIndex());
                } else if (headerText.contains("읍면동") || headerText.contains("name3")) {
                    columnMap.put("name3", cell.getColumnIndex());
                } else if (headerText.contains("격자x") || headerText.contains("nx")) {
                    columnMap.put("nx", cell.getColumnIndex());
                } else if (headerText.contains("격자y") || headerText.contains("ny")) {
                    columnMap.put("ny", cell.getColumnIndex());
                } else if (headerText.contains("경도") || headerText.contains("longitude")) {
                    columnMap.put("longitude", cell.getColumnIndex());
                } else if (headerText.contains("위도") || headerText.contains("latitude")) {
                    columnMap.put("latitude", cell.getColumnIndex());
                }
            }
        }
        
        log.debug("엑셀 헤더 매핑: {}", columnMap);
        return columnMap;
    }
    
    /**
     * 엑셀 행에서 지역 데이터 파싱
     */
    private RegionGridMapping parseRegionFromRow(Row row, Map<String, Integer> columnMap) {
        if (row == null) return null;
        
        String code = getCellValue(row, columnMap, "code");
        String name1 = getCellValue(row, columnMap, "name1");
        String name2 = getCellValue(row, columnMap, "name2");
        String name3 = getCellValue(row, columnMap, "name3");
        
        // 필수 필드 검증
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("코드가 누락되었습니다");
        }
        
        if (name1 == null || name1.trim().isEmpty()) {
            throw new IllegalArgumentException("시도명이 누락되었습니다");
        }
        
        RegionGridMapping region = new RegionGridMapping();
        region.setCode(code.trim());
        region.setName1(name1.trim());
        region.setName2(name2 != null ? name2.trim() : null);
        region.setName3(name3 != null ? name3.trim() : null);
        
        // 격자 좌표
        String nxStr = getCellValue(row, columnMap, "nx");
        String nyStr = getCellValue(row, columnMap, "ny");
        
        if (nxStr != null && !nxStr.trim().isEmpty()) {
            try {
                region.setNx(Integer.parseInt(nxStr.trim()));
            } catch (NumberFormatException e) {
                log.warn("격자 X 좌표 파싱 실패: {}", nxStr);
            }
        }
        
        if (nyStr != null && !nyStr.trim().isEmpty()) {
            try {
                region.setNy(Integer.parseInt(nyStr.trim()));
            } catch (NumberFormatException e) {
                log.warn("격자 Y 좌표 파싱 실패: {}", nyStr);
            }
        }
        
        // 경위도
        String longitudeStr = getCellValue(row, columnMap, "longitude");
        String latitudeStr = getCellValue(row, columnMap, "latitude");
        
        if (longitudeStr != null && !longitudeStr.trim().isEmpty()) {
            try {
                region.setLongitude(Double.parseDouble(longitudeStr.trim()));
            } catch (NumberFormatException e) {
                log.warn("경도 파싱 실패: {}", longitudeStr);
            }
        }
        
        if (latitudeStr != null && !latitudeStr.trim().isEmpty()) {
            try {
                region.setLatitude(Double.parseDouble(latitudeStr.trim()));
            } catch (NumberFormatException e) {
                log.warn("위도 파싱 실패: {}", latitudeStr);
            }
        }
        
        region.setCreatedAt(LocalDateTime.now());
        region.setUpdatedAt(LocalDateTime.now());
        
        return region;
    }
    
    /**
     * 셀 값 추출 헬퍼 메서드
     */
    private String getCellValue(Row row, Map<String, Integer> columnMap, String columnName) {
        Integer columnIndex = columnMap.get(columnName);
        if (columnIndex == null) return null;
        
        Cell cell = row.getCell(columnIndex);
        return getCellValueAsString(cell);
    }
    
    /**
     * 셀 값을 문자열로 변환
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 숫자를 정수로 변환 (소수점 제거)
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    /**
     * 공공데이터 API에서 대기질 측정소 파싱
     */
    public Map<String, Object> parseAirStationsFromApi() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 기존 데이터 삭제
            airStationRepository.deleteAll();
            log.info("기존 대기질 측정소 데이터 삭제 완료");
            
            int totalStations = 0;
            int successCount = 0;
            int errorCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            // 시도별로 측정소 정보 수집
            String[] cities = {
                "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
                "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
            };
            
            for (String city : cities) {
                try {
                    List<AirStation> cityStations = fetchAirStationsByCity(city);
                    airStationRepository.saveAll(cityStations);
                    
                    totalStations += cityStations.size();
                    successCount += cityStations.size();
                    
                    log.info("{} 측정소 {}개 저장 완료", city, cityStations.size());
                    
                    // API 호출 간격 조절
                    Thread.sleep(500);
                    
                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = String.format("%s 측정소 파싱 실패: %s", city, e.getMessage());
                    errorMessages.add(errorMsg);
                    log.error(errorMsg, e);
                }
            }
            
            result.put("totalStations", totalStations);
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            result.put("errorMessages", errorMessages);
            
            log.info("대기질 측정소 파싱 완료 - 총 {}개 처리", totalStations);
            
        } catch (Exception e) {
            log.error("대기질 측정소 파싱 실패", e);
            throw new RuntimeException("대기질 측정소 파싱 중 오류 발생: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 시도별 대기질 측정소 목록 조회
     */
    private List<AirStation> fetchAirStationsByCity(String city) throws Exception {
        String url = UriComponentsBuilder.fromHttpUrl(airKoreaBaseUrl)
                .path("/ArpltnInforInqireSvc/getMsrstnList")
                .queryParam("serviceKey", airKoreaServiceKey)
                .queryParam("returnType", "xml")
                .queryParam("numOfRows", "100")
                .queryParam("pageNo", "1")
                .queryParam("addr", city)
                .build()
                .toUriString();
        
        log.debug("측정소 목록 API 호출: {}", url);
        
        String response = restTemplate.getForObject(url, String.class);
        return parseAirStationXml(response, city);
    }
    
    /**
     * 대기질 측정소 XML 파싱
     */
    private List<AirStation> parseAirStationXml(String xmlResponse, String city) throws Exception {
        List<AirStation> stations = new ArrayList<>();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes()));
        
        NodeList itemNodes = doc.getElementsByTagName("item");
        
        for (int i = 0; i < itemNodes.getLength(); i++) {
            Node itemNode = itemNodes.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) itemNode;
                
                String stationName = getElementText(itemElement, "stationName");
                String addr = getElementText(itemElement, "addr");
                String dmX = getElementText(itemElement, "dmX");
                String dmY = getElementText(itemElement, "dmY");
                
                // 측정소 코드 생성
                String stationCode = generateStationCode(stationName, city);
                
                AirStation station = new AirStation();
                station.setStationName(stationName);
                station.setStationSigu(city);
                station.setStationCode(stationCode);
                station.setAddr(addr);
                
                // 좌표 변환 (TM -> WGS84 근사)
                if (dmX != null && !dmX.trim().isEmpty() && dmY != null && !dmY.trim().isEmpty()) {
                    try {
                        double x = Double.parseDouble(dmX);
                        double y = Double.parseDouble(dmY);
                        
                        // 간단한 좌표 변환 (정확하지 않지만 근사치)
                        double longitude = x / 1000000.0 + 126.0;
                        double latitude = y / 1000000.0 + 35.0;
                        
                        station.setLongitude(String.valueOf(longitude));
                        station.setLatitude(String.valueOf(latitude));
                    } catch (NumberFormatException e) {
                        log.warn("좌표 변환 실패: {} - {}, {}", stationName, dmX, dmY);
                    }
                }
                
                station.setCreatedAt(LocalDateTime.now());
                stations.add(station);
            }
        }
        
        return stations;
    }
    
    /**
     * XML 요소 텍스트 추출
     */
    private String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node != null ? node.getTextContent() : "";
        }
        return "";
    }
    
    /**
     * 측정소 코드 생성
     */
    private String generateStationCode(String stationName, String city) {
        if (stationName == null || city == null) {
            return "ST" + System.currentTimeMillis() % 100000;
        }
        
        String combined = city + "_" + stationName;
        int hash = Math.abs(combined.hashCode());
        return String.format("%06d", hash % 1000000);
    }
    
    /**
     * 테스트 데이터 생성
     */
    public Map<String, Object> createSampleData() {
        Map<String, Object> result = new HashMap<>();
        
        // 샘플 지역 데이터
        List<RegionGridMapping> sampleRegions = Arrays.asList(
            createSampleRegion("1111000000", "서울특별시", "종로구", null, 60, 127, 126.9676, 37.5689),
            createSampleRegion("1168000000", "서울특별시", "강남구", null, 61, 126, 127.0473, 37.5161),
            createSampleRegion("2611000000", "부산광역시", "중구", null, 98, 76, 129.0331, 35.1030),
            createSampleRegion("2726000000", "대구광역시", "달서구", null, 88, 90, 128.5350, 35.8294),
            createSampleRegion("2817000000", "인천광역시", "남동구", null, 56, 124, 126.7318, 37.4636)
        );
        
        regionRepository.saveAll(sampleRegions);
        
        // 샘플 대기질 측정소
        List<AirStation> sampleStations = Arrays.asList(
            createSampleAirStation("111123", "중구", "서울", "서울 중구 덕수궁길 15", "126.9748", "37.5636"),
            createSampleAirStation("111181", "강남구", "서울", "서울 강남구 테헤란로 114", "127.0594", "37.5048"),
            createSampleAirStation("211121", "중구", "부산", "부산 중구 중구로 120", "129.0332", "35.1031"),
            createSampleAirStation("271121", "달서구", "대구", "대구 달서구 달구벌대로 1525", "128.5351", "35.8295"),
            createSampleAirStation("281121", "남동구", "인천", "인천 남동구 구월남로 16", "126.7319", "37.4637")
        );
        
        airStationRepository.saveAll(sampleStations);
        
        result.put("regionCount", sampleRegions.size());
        result.put("stationCount", sampleStations.size());
        
        return result;
    }
    
    private RegionGridMapping createSampleRegion(String code, String name1, String name2, String name3, 
                                                  Integer nx, Integer ny, Double longitude, Double latitude) {
        RegionGridMapping region = new RegionGridMapping();
        region.setCode(code);
        region.setName1(name1);
        region.setName2(name2);
        region.setName3(name3);
        region.setNx(nx);
        region.setNy(ny);
        region.setLongitude(longitude);
        region.setLatitude(latitude);
        region.setCreatedAt(LocalDateTime.now());
        region.setUpdatedAt(LocalDateTime.now());
        return region;
    }
    
    private AirStation createSampleAirStation(String stationCode, String stationName, String stationSigu, 
                                              String addr, String longitude, String latitude) {
        AirStation station = new AirStation();
        station.setStationCode(stationCode);
        station.setStationName(stationName);
        station.setStationSigu(stationSigu);
        station.setAddr(addr);
        station.setLongitude(longitude);
        station.setLatitude(latitude);
        station.setCreatedAt(LocalDateTime.now());
        return station;
    }
    
    /**
     * 전체 데이터 초기화
     */
    public void resetAllData() {
        regionRepository.deleteAll();
        airStationRepository.deleteAll();
        log.info("전체 데이터 초기화 완료");
    }
    
    /**
     * 데이터 통계 조회
     */
    public Map<String, Object> getDataStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long regionCount = regionRepository.count();
        long airStationCount = airStationRepository.count();
        
        stats.put("regionCount", regionCount);
        stats.put("airStationCount", airStationCount);
        stats.put("lastUpdated", LocalDateTime.now());
        
        return stats;
    }
}