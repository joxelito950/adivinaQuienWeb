package com.joxelito.adivinaquien.unit;

import com.joxelito.adivinaquien.config.AppProperties;
import com.joxelito.adivinaquien.domain.Difficulty;
import com.joxelito.adivinaquien.domain.PlayerType;
import com.joxelito.adivinaquien.matchmaking.MatchStarted;
import com.joxelito.adivinaquien.matchmaking.MatchmakingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MatchmakingServiceTest {

    private MatchmakingService service;

    @AfterEach
    void tearDown() {
        if (service != null) {
            service.shutdown();
        }
    }

    @Test
    void pairsTwoHumansInSameDifficulty() {
        AppProperties properties = new AppProperties();
        properties.setMatchTimeoutSeconds(1);
        service = new MatchmakingService(properties);

        AtomicReference<MatchStarted> ref = new AtomicReference<>();
        service.registerListener(ref::set);

        service.joinQueue("p1", "s1", Difficulty.MEDIUM);
        service.joinQueue("p2", "s2", Difficulty.MEDIUM);

        MatchStarted started = ref.get();
        assertNotNull(started);
        assertEquals(PlayerType.HUMAN, started.first().type());
        assertEquals(PlayerType.HUMAN, started.second().type());
    }

    @Test
    void createsDummyAfterTimeout() {
        AppProperties properties = new AppProperties();
        properties.setMatchTimeoutSeconds(1);
        service = new MatchmakingService(properties);

        AtomicReference<MatchStarted> ref = new AtomicReference<>();
        service.registerListener(ref::set);

        service.joinQueue("p1", "s1", Difficulty.SMALL);

        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .until(() -> ref.get() != null);

        MatchStarted started = ref.get();
        assertNotNull(started);
        assertEquals(PlayerType.DUMMY, started.second().type());
    }

    @Test
    void startsDummyImmediatelyWhenRequested() {
        AppProperties properties = new AppProperties();
        properties.setMatchTimeoutSeconds(60);
        service = new MatchmakingService(properties);

        AtomicReference<MatchStarted> ref = new AtomicReference<>();
        service.registerListener(ref::set);

        service.joinQueue("p1", "s1", Difficulty.SMALL);
        boolean startedNow = service.startMatchWithDummyNow("p1");

        assertEquals(true, startedNow);
        MatchStarted started = ref.get();
        assertNotNull(started);
        assertEquals(PlayerType.HUMAN, started.first().type());
        assertEquals(PlayerType.DUMMY, started.second().type());
    }
}

