package com.joxelito.adivinaquien.matchmaking;

import com.joxelito.adivinaquien.config.AppProperties;
import com.joxelito.adivinaquien.domain.Difficulty;
import com.joxelito.adivinaquien.domain.PlayerType;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class MatchmakingService {

    private final long timeoutSeconds;
    private final ScheduledExecutorService scheduler;
    private final Map<Difficulty, Deque<QueueEntry>> queueByDifficulty = new ConcurrentHashMap<>();
    private final Map<String, QueueEntry> queueEntryByPlayer = new ConcurrentHashMap<>();

    private volatile MatchListener matchListener;

    public MatchmakingService(AppProperties appProperties) {
        this.timeoutSeconds = appProperties.getMatchTimeoutSeconds();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        for (Difficulty difficulty : Difficulty.values()) {
            queueByDifficulty.put(difficulty, new ArrayDeque<>());
        }
    }

    public void registerListener(MatchListener listener) {
        this.matchListener = listener;
    }

    public synchronized boolean joinQueue(String playerId, String socketSessionId, Difficulty difficulty) {
        Objects.requireNonNull(playerId);
        Objects.requireNonNull(difficulty);

        if (queueEntryByPlayer.containsKey(playerId)) {
            return false;
        }

        Deque<QueueEntry> queue = queueByDifficulty.get(difficulty);
        QueueEntry waiting = queue.peekFirst();
        if (waiting != null) {
            queue.removeFirst();
            queueEntryByPlayer.remove(waiting.playerId());
            waiting.cancelTimeout();

            MatchStarted match = new MatchStarted(
                    difficulty,
                    new MatchParticipant(waiting.playerId(), waiting.socketSessionId(), PlayerType.HUMAN),
                    new MatchParticipant(playerId, socketSessionId, PlayerType.HUMAN)
            );
            notifyMatchStarted(match);
            return true;
        }

        QueueEntry entry = new QueueEntry(playerId, socketSessionId, difficulty);
        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> triggerDummy(entry), timeoutSeconds, TimeUnit.SECONDS);
        entry.attachTimeout(timeoutTask);
        queue.addLast(entry);
        queueEntryByPlayer.put(playerId, entry);
        return true;
    }

    public synchronized void leaveQueue(String playerId) {
        QueueEntry entry = queueEntryByPlayer.remove(playerId);
        if (entry == null) {
            return;
        }
        queueByDifficulty.get(entry.difficulty()).remove(entry);
        entry.cancelTimeout();
    }

    public synchronized boolean startMatchWithDummyNow(String playerId) {
        Objects.requireNonNull(playerId);

        QueueEntry entry = queueEntryByPlayer.get(playerId);
        if (entry == null) {
            return false;
        }

        entry.cancelTimeout();
        triggerDummy(entry);
        return true;
    }

    private synchronized void triggerDummy(QueueEntry entry) {
        if (!queueEntryByPlayer.containsKey(entry.playerId())) {
            return;
        }
        queueEntryByPlayer.remove(entry.playerId());
        queueByDifficulty.get(entry.difficulty()).remove(entry);

        MatchStarted match = new MatchStarted(
                entry.difficulty(),
                new MatchParticipant(entry.playerId(), entry.socketSessionId(), PlayerType.HUMAN),
                new MatchParticipant("dummy-" + UUID.randomUUID(), null, PlayerType.DUMMY)
        );
        notifyMatchStarted(match);
    }

    private void notifyMatchStarted(MatchStarted matchStarted) {
        MatchListener listener = matchListener;
        if (listener != null) {
            listener.onMatchStarted(matchStarted);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    private static final class QueueEntry {

        private final String playerId;
        private final String socketSessionId;
        private final Difficulty difficulty;
        private ScheduledFuture<?> timeout;

        private QueueEntry(String playerId, String socketSessionId, Difficulty difficulty) {
            this.playerId = playerId;
            this.socketSessionId = socketSessionId;
            this.difficulty = difficulty;
        }

        String playerId() {
            return playerId;
        }

        String socketSessionId() {
            return socketSessionId;
        }

        Difficulty difficulty() {
            return difficulty;
        }

        void attachTimeout(ScheduledFuture<?> timeout) {
            this.timeout = timeout;
        }

        void cancelTimeout() {
            if (timeout != null) {
                timeout.cancel(false);
            }
        }
    }
}

