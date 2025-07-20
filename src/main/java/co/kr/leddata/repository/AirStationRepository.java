package co.kr.leddata.repository;

import co.kr.leddata.entity.AirStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirStationRepository extends JpaRepository<AirStation, Long> {
    
    // 측정소명으로 검색 (부분 매칭)
    @Query("SELECT a FROM AirStation a WHERE " +
           "a.stationName LIKE %:keyword% OR " +
           "a.stationSigu LIKE %:keyword% OR " +
           "a.addr LIKE %:keyword% " +
           "ORDER BY a.stationName")
    List<AirStation> findByStationNameContaining(@Param("keyword") String keyword);
    
    // 시/군/구로 검색
    @Query("SELECT a FROM AirStation a WHERE a.stationSigu = :sigu ORDER BY a.stationName")
    List<AirStation> findByStationSigu(@Param("sigu") String sigu);
    
    // 측정소명으로 정확히 찾기
    Optional<AirStation> findByStationName(String stationName);
    
    // 측정소 코드로 찾기
    Optional<AirStation> findByStationCode(String stationCode);
    
    // 모든 시/군/구 목록
    @Query("SELECT DISTINCT a.stationSigu FROM AirStation a ORDER BY a.stationSigu")
    List<String> findDistinctStationSigu();
}