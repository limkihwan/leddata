# LED 환경정보 서비스 프로젝트

## 🚀 프로젝트 구조
```
src/main/java/co/kr/leddata/
├── LeddataApplication.java     # 메인 애플리케이션 (스케줄러 활성화)
├── config/
│   └── AppConfig.java         # RestTemplate 설정
├── controller/
│   ├── PlayerController.java  # 플레이어 페이지 컨트롤러
│   └── ApiController.java     # REST API 컨트롤러
├── entity/
│   ├── WeatherData.java      # 날씨 데이터 엔티티
│   └── AirData.java          # 대기질 데이터 엔티티
├── repository/
│   ├── WeatherRepository.java # 날씨 데이터 저장소
│   └── AirRepository.java     # 대기질 데이터 저장소
└── service/                   # 서비스 클래스들 (추후 추가)
```

## 📋 현재 완성된 기능
✅ Spring Boot 3.5.3 프로젝트 생성
✅ H2 인메모리 데이터베이스 설정
✅ JPA Entity 및 Repository 구성
✅ 플레이어별 환경정보 표시 웹페이지
✅ REST API 엔드포인트
✅ 스케줄러 기반 구조 준비
✅ 10개 플레이어 설정 파일

## 🔧 실행 방법
1. IntelliJ에서 프로젝트 열기
2. `LeddataApplication.java` 실행
3. 브라우저에서 접속:
   - 메인: http://localhost:8080/player/240025
   - API: http://localhost:8080/api/status
   - DB콘솔: http://localhost:8080/h2-console

## 🎯 다음 단계 (공공API 연동)
1. WeatherService.java - 기상청 API 연동
2. AirService.java - 대기질 API 연동  
3. DataCollectorService.java - 스케줄러 구현
4. API 키 설정

## 📱 플레이어 목록
- 240025: 서울시청
- 240026: 부산시청
- 240027: 대구시청
- 240028: 인천시청
- 240029: 광주시청
- 240030: 대전시청
- 240031: 울산시청
- 240032: 세종시청
- 240033: 수원시청
- 240034: 춘천시청

지금 바로 실행해서 테스트할 수 있습니다!