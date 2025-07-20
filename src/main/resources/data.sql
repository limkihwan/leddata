-- H2 데이터베이스 초기 설정
-- Player 테이블 스키마 업데이트

-- 기존 resolution 컬럼이 있다면 데이터 마이그레이션
UPDATE players SET 
    resolution_width = CAST(SUBSTRING(resolution, 1, LOCATE('x', resolution) - 1) AS INTEGER),
    resolution_height = CAST(SUBSTRING(resolution, LOCATE('x', resolution) + 1) AS INTEGER)
WHERE resolution IS NOT NULL AND resolution LIKE '%x%';

-- 기본 플레이어 데이터 삽입 (이미 존재하지 않는 경우만)
INSERT INTO players (player_code, player_name, region, resolution_width, resolution_height, 
                    weather_nx, weather_ny, air_station_name, forest_region_code, theme, 
                    access_allowed, connection_status, created_at, updated_at,
                    service_start_date, service_end_date) 
SELECT * FROM (
    VALUES 
    ('240025', '테스트 플레이어 1', '서울 강남구', 1920, 1080, 61, 126, '중구', '11', 'default', true, 'DISCONNECTED', NOW(), NOW(), NULL, NULL),
    ('240026', '테스트 플레이어 2', '서울 종로구', 1366, 768, 60, 127, '종로구', '11', 'blue', true, 'DISCONNECTED', NOW(), NOW(), NULL, NULL),
    ('240027', '테스트 플레이어 3', '부산 해운대구', 1920, 1080, 99, 74, '부산', '26', 'green', true, 'DISCONNECTED', NOW(), NOW(), NULL, NULL),
    ('240028', '테스트 플레이어 4', '대구 중구', 1280, 720, 89, 90, '대구', '27', 'red', true, 'DISCONNECTED', NOW(), NOW(), NULL, NULL),
    ('240029', '테스트 플레이어 5', '인천 중구', 3840, 2160, 55, 124, '인천', '28', 'purple', true, 'DISCONNECTED', NOW(), NOW(), NULL, NULL),
    ('240030', '테스트 플레이어 6', '광주 동구', 1920, 1080, 58, 74, '광주', '29', 'orange', true, 'DISCONNECTED', NOW(), NOW(), NULL, NULL),
    ('240031', '테스트 플레이어 7', '대전 유성구', 2560, 1440, 67, 100, '대전', '30', 'pink', true, 'DISCONNECTED', NOW(), NOW(), NULL, NULL),
    ('240032', '테스트 플레이어 8', '울산 남구', 1920, 1080, 102, 84, '울산', '31', 'cyan', true, 'DISCONNECTED', NOW(), NOW(), NULL, NULL),
    ('240033', '테스트 플레이어 9', '세종시', 1366, 768, 66, 103, '세종', '36', 'lime', true, 'DISCONNECTED', NOW(), NOW(), NULL, NULL),
    ('240034', '테스트 플레이어 10', '제주시', 1920, 1080, 52, 38, '제주', '50', 'teal', true, 'DISCONNECTED', NOW(), NOW(), NULL, NULL)
) AS t(player_code, player_name, region, resolution_width, resolution_height, weather_nx, weather_ny, air_station_name, forest_region_code, theme, access_allowed, connection_status, created_at, updated_at, service_start_date, service_end_date)
WHERE NOT EXISTS (SELECT 1 FROM players WHERE players.player_code = t.player_code);
