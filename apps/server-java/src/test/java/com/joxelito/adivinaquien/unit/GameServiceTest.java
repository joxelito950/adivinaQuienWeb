package com.joxelito.adivinaquien.unit;

import com.joxelito.adivinaquien.concurrency.GameLockManager;
import com.joxelito.adivinaquien.config.AppProperties;
import com.joxelito.adivinaquien.domain.Difficulty;
import com.joxelito.adivinaquien.domain.GameSession;
import com.joxelito.adivinaquien.domain.PlayerType;
import com.joxelito.adivinaquien.engine.BoardFactory;
import com.joxelito.adivinaquien.engine.CharacterCatalog;
import com.joxelito.adivinaquien.engine.GameService;
import com.joxelito.adivinaquien.engine.GuessResult;
import com.joxelito.adivinaquien.matchmaking.MatchParticipant;
import com.joxelito.adivinaquien.matchmaking.MatchStarted;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameServiceTest {

    private GameService gameService;

    @AfterEach
    void tearDown() {
        if (gameService != null) {
            gameService.shutdown();
        }
    }

    @Test
    void wrongGuessOnlyChangesTurn() {
        AppProperties properties = new AppProperties();
        properties.setReconnectTimeoutSeconds(1);

        CharacterCatalog catalog = new CharacterCatalog();
        BoardFactory boardFactory = new BoardFactory(catalog);
        gameService = new GameService(boardFactory, new GameLockManager(), properties);

        MatchStarted started = new MatchStarted(
                Difficulty.SMALL,
                new MatchParticipant("p1", "s1", PlayerType.HUMAN),
                new MatchParticipant("p2", "s2", PlayerType.HUMAN)
        );

        GameSession game = gameService.createFromMatch(started);
        String p1 = game.getPlayerOne().getPlayerId();

        GuessResult result = gameService.guessCharacter(game.getGameId(), p1, "char-not-valid");

        assertFalse(result.correct());
        assertFalse(result.finished());
        assertTrue(result.nextTurnPlayerId().equals("p1") || result.nextTurnPlayerId().equals("p2"));
        assertNotEquals(p1, result.nextTurnPlayerId());
    }
}

