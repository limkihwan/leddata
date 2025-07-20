package co.kr.leddata.repository;

import co.kr.leddata.entity.RegionGridMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionGridMappingRepository extends JpaRepository<RegionGridMapping, Long> {
    
    // 지역명으로 검색 (부분 매칭)
    @Query("SELECT r FROM RegionGridMapping r WHERE " +
           "r.name1 LIKE %:keyword% OR " +
           "r.name2 LIKE %:keyword% OR " +
           "r.name3 LIKE %:keyword% " +
           "ORDER BY r.name1, r.name2, r.name3")
    List<RegionGridMapping> findByRegionNameContaining(@Param("keyword") String keyword);
    
    // 시/도로 검색
    @Query("SELECT r FROM RegionGridMapping r WHERE r.name1 = :name1 ORDER BY r.name2, r.name3")
    List<RegionGridMapping> findByName1(@Param("name1") String name1);
    
    // 시/도 + 시/군/구로 검색
    @Query("SELECT r FROM RegionGridMapping r WHERE r.name1 = :name1 AND r.name2 = :name2 ORDER BY r.name3")
    List<RegionGridMapping> findByName1AndName2(@Param("name1") String name1, @Param("name2") String name2);
    
    // 코드로 검색
    Optional<RegionGridMapping> findByCode(String code);
    
    // 좌표로 검색
    Optional<RegionGridMapping> findByNxAndNy(Integer nx, Integer ny);
    
    // 모든 시/도 목록
    @Query("SELECT DISTINCT r.name1 FROM RegionGridMapping r WHERE r.name1 != 'nan' ORDER BY r.name1")
    List<String> findDistinctName1();
    
    // 특정 시/도의 시/군/구 목록
    @Query("SELECT DISTINCT r.name2 FROM RegionGridMapping r WHERE r.name1 = :name1 AND r.name2 != 'nan' ORDER BY r.name2")
    List<String> findDistinctName2ByName1(@Param("name1") String name1);
}