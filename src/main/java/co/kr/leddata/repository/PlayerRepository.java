package co.kr.leddata.repository;

import co.kr.leddata.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {
    
    // 플레이어 코드로 조회
    Optional<Player> findByPlayerCode(String playerCode);
    
    // 접속 상태별 조회
    List<Player> findByConnectionStatus(String connectionStatus);
    
    // 접속 허가된 플레이어들
    List<Player> findByAccessAllowedTrue();
    
    // 테마별 플레이어 조회
    List<Player> findByTheme(String theme);
    
    // 현재 접속 중인 플레이어들
    @Query("SELECT p FROM Player p WHERE p.connectionStatus = 'CONNECTED'")
    List<Player> findConnectedPlayers();
    
    // 접속 대기 중인 플레이어들
    @Query("SELECT p FROM Player p WHERE p.connectionStatus = 'WAITING'")
    List<Player> findWaitingPlayers();
}
