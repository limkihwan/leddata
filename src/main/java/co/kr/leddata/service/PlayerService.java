package co.kr.leddata.service;

import co.kr.leddata.entity.Player;
import co.kr.leddata.entity.PlayerSession;
import co.kr.leddata.repository.PlayerRepository;
import co.kr.leddata.repository.PlayerSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PlayerService {
    
    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private PlayerSessionRepository sessionRepository;
    
    // 모든 플레이어 조회
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
    
    // 플레이어 코드로 조회
    public Optional<Player> getPlayerByCode(String playerCode) {
        return playerRepository.findByPlayerCode(playerCode);
    }
    
    // 플레이어 등록/수정
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }
    
    // 플레이어 삭제
    public void deletePlayer(String playerCode) {
        // 관련 세션들도 모두 정리
        List<PlayerSession> sessions = sessionRepository.findByPlayerCode(playerCode);
        for (PlayerSession session : sessions) {
            session.setStatus("DISCONNECTED");
            session.setDisconnectedAt(LocalDateTime.now());
        }
        sessionRepository.saveAll(sessions);
        
        playerRepository.deleteById(playerCode);
        log.info("플레이어 삭제 완료: {}", playerCode);
    }
    
    // 플레이어 접속 시도
    public String attemptConnection(String playerCode, String ipAddress, String userAgent) {
        Optional<Player> playerOpt = playerRepository.findByPlayerCode(playerCode);
        if (!playerOpt.isPresent()) {
            throw new RuntimeException("존재하지 않는 플레이어 코드입니다: " + playerCode);
        }
        
        Player player = playerOpt.get();
        
        // 접속 허가 확인
        if (!player.getAccessAllowed()) {
            throw new RuntimeException("접속이 차단된 플레이어입니다: " + playerCode);
        }
        
        String sessionId = UUID.randomUUID().toString();
        PlayerSession session = new PlayerSession(sessionId, playerCode, ipAddress);
        session.setUserAgent(userAgent);
        
        // 기존 활성 세션 확인
        List<PlayerSession> activeSessions = sessionRepository.findActiveSessionsByPlayerCode(playerCode);
        
        if (activeSessions.isEmpty()) {
            // 첫 번째 접속 - 즉시 연결
            session.setStatus("ACTIVE");
            player.setConnectionStatus("CONNECTED");
            player.setCurrentSessionId(sessionId);
            player.setLastConnectedAt(LocalDateTime.now());
            
            log.info("플레이어 접속 승인: {} ({})", playerCode, ipAddress);
        } else {
            // 이미 활성 세션 존재 - 대기 상태
            session.setStatus("WAITING");
            player.setConnectionStatus("WAITING");
            
            log.info("플레이어 접속 대기: {} ({})", playerCode, ipAddress);
        }
        
        sessionRepository.save(session);
        playerRepository.save(player);
        
        return sessionId;
    }
    
    // 세션 활성화 (관리자가 승인)
    public void activateSession(String sessionId) {
        Optional<PlayerSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new RuntimeException("존재하지 않는 세션입니다: " + sessionId);
        }
        
        PlayerSession session = sessionOpt.get();
        String playerCode = session.getPlayerCode();
        
        // 기존 활성 세션 비활성화
        List<PlayerSession> activeSessions = sessionRepository.findActiveSessionsByPlayerCode(playerCode);
        for (PlayerSession activeSession : activeSessions) {
            activeSession.setStatus("DISCONNECTED");
            activeSession.setDisconnectedAt(LocalDateTime.now());
        }
        sessionRepository.saveAll(activeSessions);
        
        // 새 세션 활성화
        session.setStatus("ACTIVE");
        sessionRepository.save(session);
        
        // 플레이어 상태 업데이트
        Optional<Player> playerOpt = playerRepository.findByPlayerCode(playerCode);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setConnectionStatus("CONNECTED");
            player.setCurrentSessionId(sessionId);
            player.setLastConnectedAt(LocalDateTime.now());
            playerRepository.save(player);
        }
        
        log.info("세션 활성화 완료: {} (플레이어: {})", sessionId, playerCode);
    }
    
    // 세션 차단
    public void blockSession(String sessionId) {
        Optional<PlayerSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new RuntimeException("존재하지 않는 세션입니다: " + sessionId);
        }
        
        PlayerSession session = sessionOpt.get();
        session.setStatus("DISCONNECTED");
        session.setDisconnectedAt(LocalDateTime.now());
        sessionRepository.save(session);
        
        // 플레이어 상태 업데이트
        String playerCode = session.getPlayerCode();
        Optional<Player> playerOpt = playerRepository.findByPlayerCode(playerCode);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setConnectionStatus("DISCONNECTED");
            player.setCurrentSessionId(null);
            playerRepository.save(player);
        }
        
        log.info("세션 차단 완료: {} (플레이어: {})", sessionId, playerCode);
    }
    
    // 플레이어별 세션 목록
    public List<PlayerSession> getPlayerSessions(String playerCode) {
        return sessionRepository.findByPlayerCode(playerCode);
    }
    
    // 접속 허가/차단 토글
    public void togglePlayerAccess(String playerCode) {
        Optional<Player> playerOpt = playerRepository.findByPlayerCode(playerCode);
        if (!playerOpt.isPresent()) {
            throw new RuntimeException("존재하지 않는 플레이어입니다: " + playerCode);
        }
        
        Player player = playerOpt.get();
        player.setAccessAllowed(!player.getAccessAllowed());
        
        // 접속 차단 시 모든 세션 종료
        if (!player.getAccessAllowed()) {
            List<PlayerSession> sessions = sessionRepository.findByPlayerCode(playerCode);
            for (PlayerSession session : sessions) {
                if (!"DISCONNECTED".equals(session.getStatus())) {
                    session.setStatus("DISCONNECTED");
                    session.setDisconnectedAt(LocalDateTime.now());
                }
            }
            sessionRepository.saveAll(sessions);
            
            player.setConnectionStatus("DISCONNECTED");
            player.setCurrentSessionId(null);
        }
        
        playerRepository.save(player);
        log.info("플레이어 접속 권한 변경: {} -> {}", playerCode, player.getAccessAllowed() ? "허가" : "차단");
    }
    
    // 비활성 세션 정리 (스케줄러용)
    public void cleanupInactiveSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30); // 30분 비활성
        List<PlayerSession> inactiveSessions = sessionRepository.findInactiveSessions(cutoffTime);
        
        for (PlayerSession session : inactiveSessions) {
            session.setStatus("DISCONNECTED");
            session.setDisconnectedAt(LocalDateTime.now());
            
            // 플레이어 상태도 업데이트
            if ("ACTIVE".equals(session.getStatus())) {
                Optional<Player> playerOpt = playerRepository.findByPlayerCode(session.getPlayerCode());
                if (playerOpt.isPresent()) {
                    Player player = playerOpt.get();
                    player.setConnectionStatus("DISCONNECTED");
                    player.setCurrentSessionId(null);
                    playerRepository.save(player);
                }
            }
        }
        
        sessionRepository.saveAll(inactiveSessions);
        log.info("비활성 세션 정리 완료: {}개", inactiveSessions.size());
    }
}
