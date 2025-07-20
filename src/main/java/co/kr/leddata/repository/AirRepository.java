package co.kr.leddata.repository;

import co.kr.leddata.entity.AirData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AirRepository extends JpaRepository<AirData, Long> {
    
    // 플레이어 코드로 대기질 데이터 조회
    Optional<AirData> findByPlayerCode(String playerCode);
    
    // 플레이어 코드로 대기질 데이터 삭제 (중복 방지용)
    @Modifying
    @Transactional
    @Query("DELETE FROM AirData a WHERE a.playerCode = :playerCode")
    void deleteByPlayerCode(@Param("playerCode") String playerCode);
    
    // 측정소명으로 대기질 데이터 조회
    Optional<AirData> findByStationName(String stationName);
    
    // 특정 지역의 대기질 데이터 조회
    Optional<AirData> findByRegion(String region);
    
    // 플레이어 코드 존재 여부 확인
    boolean existsByPlayerCode(String playerCode);
}