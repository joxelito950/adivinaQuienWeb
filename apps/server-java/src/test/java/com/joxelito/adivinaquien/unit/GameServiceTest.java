package com.joxelito.adivinaquien.unit;

import com.joxelito.adivinaquien.concurrency.GameLockManager;
import com.joxelito.adivinaquien.config.AppProperties;
import com.joxelito.adivinaquien.domain.Difficulty;
import com.joxelito.adivinaquien.domain.GameSession;
import com.joxelito.adivinaquien.domain.PlayerType;
import com.joxelito.adivinaquien.domain.QuestionKey;
import com.joxelito.adivinaquien.engine.BoardFactory;
import com.joxelito.adivinaquien.engine.CharacterCatalog;
import com.joxelito.adivinaquien.engine.GameService;
import com.joxelito.adivinaquien.engine.GuessResult;
import com.joxelito.adivinaquien.engine.PendingQuestionPrompt;
import com.joxelito.adivinaquien.engine.QuestionResult;
import com.joxelito.adivinaquien.matchmaking.MatchParticipant;
import com.joxelito.adivinaquien.matchmaking.MatchStarted;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void pendingQuestionRequiresDefenderAnswer() {
        AppProperties properties = new AppProperties();
        properties.setReconnectTimeoutSeconds(1);
        properties.setQuestionResponseTimeoutSeconds(15);

        CharacterCatalog catalog = new CharacterCatalog();
        BoardFactory boardFactory = new BoardFactory(catalog);
        gameService = new GameService(boardFactory, new GameLockManager(), properties);

        MatchStarted started = new MatchStarted(
                Difficulty.SMALL,
                new MatchParticipant("p1", "s1", PlayerType.HUMAN),
                new MatchParticipant("p2", "s2", PlayerType.HUMAN)
        );

        GameSession game = gameService.createFromMatch(started);
        PendingQuestionPrompt prompt = gameService.askQuestion(game.getGameId(), "p1", QuestionKey.HAS_BEARD);

        assertNotNull(prompt);
        assertNotNull(gameService.getPendingQuestion(game.getGameId()));

        QuestionResult resolved = gameService.answerQuestion(game.getGameId(), "p2", true, false);
        assertTrue(resolved.answer());
        assertFalse(resolved.timeoutFallback());
        assertTrue("p2".equals(resolved.nextTurnPlayerId()));
    }

    @Test
    void timeoutFallbackResolvesWithCorrectServerAnswer() {
        AppProperties properties = new AppProperties();
        properties.setReconnectTimeoutSeconds(1);
        properties.setQuestionResponseTimeoutSeconds(1);

        CharacterCatalog catalog = new CharacterCatalog();
        BoardFactory boardFactory = new BoardFactory(catalog);
        gameService = new GameService(boardFactory, new GameLockManager(), properties);

        MatchStarted started = new MatchStarted(
                Difficulty.SMALL,
                new MatchParticipant("p1", "s1", PlayerType.HUMAN),
                new MatchParticipant("p2", "s2", PlayerType.HUMAN)
        );

        GameSession game = gameService.createFromMatch(started);
        gameService.askQuestion(game.getGameId(), "p1", QuestionKey.USES_GLASSES);

        QuestionResult timeoutResult = gameService.resolvePendingQuestionWithCorrectAnswer(game.getGameId(), true);
        assertNotNull(timeoutResult);
        assertTrue(timeoutResult.timeoutFallback());
        assertTrue("p2".equals(timeoutResult.nextTurnPlayerId()));
    }
}

