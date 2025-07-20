package co.kr.leddata.repository;

import co.kr.leddata.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<WeatherData, Long> {
    
    // 플레이어 코드로 날씨 데이터 조회
    Optional<WeatherData> findByPlayerCode(String playerCode);
    
    // 플레이어 코드로 날씨 데이터 삭제 (중복 방지용)
    @Modifying
    @Transactional
    @Query("DELETE FROM WeatherData w WHERE w.playerCode = :playerCode")
    void deleteByPlayerCode(@Param("playerCode") String playerCode);
    
    // 특정 지역의 날씨 데이터 조회
    Optional<WeatherData> findByRegion(String region);
    
    // 플레이어 코드 존재 여부 확인
    boolean existsByPlayerCode(String playerCode);
}