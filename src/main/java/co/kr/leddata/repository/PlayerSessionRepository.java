package co.kr.leddata.repository;

import co.kr.leddata.entity.PlayerSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerSessionRepository extends JpaRepository<PlayerSession, Long> {
    
    // 세션 ID로 조회
    Optional<PlayerSession> findBySessionId(String sessionId);
    
    // 플레이어별 세션 조회
    List<PlayerSession> findByPlayerCode(String playerCode);
    
    // 플레이어별 활성 세션 조회
    @Query("SELECT s FROM PlayerSession s WHERE s.playerCode = ?1 AND s.status = 'ACTIVE'")
    List<PlayerSession> findActiveSessionsByPlayerCode(String playerCode);
    
    // 플레이어별 대기 세션 조회
    @Query("SELECT s FROM PlayerSession s WHERE s.playerCode = ?1 AND s.status = 'WAITING'")
    List<PlayerSession> findWaitingSessionsByPlayerCode(String playerCode);
    
    // 비활성 세션 정리용
    @Query("SELECT s FROM PlayerSession s WHERE s.lastActivityAt < ?1 AND s.status != 'DISCONNECTED'")
    List<PlayerSession> findInactiveSessions(LocalDateTime cutoffTime);
    
    // 상태별 세션 조회
    List<PlayerSession> findByStatus(String status);
}
