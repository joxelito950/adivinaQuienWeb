package com.joxelito.adivinaquien.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class GameLockManager {

    private final Map<String, ReentrantLock> lockByGameId = new ConcurrentHashMap<>();

    public <T> T executeSerialized(String gameId, Supplier<T> action) {
        ReentrantLock lock = lockByGameId.computeIfAbsent(gameId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    public void executeSerialized(String gameId, Runnable action) {
        executeSerialized(gameId, () -> {
            action.run();
            return null;
        });
    }
}

