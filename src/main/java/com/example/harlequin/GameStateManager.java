package com.example.harlequin;

import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class GameStateManager {
    private int currentWave = 1;
    private int timeRemainingInWave;
    private final int WAVE_DURATION = 10;
    private final double INITIAL_SPAWN_INTERVAL = 1;
    private double spawnInterval = INITIAL_SPAWN_INTERVAL;

    private Label playerLevelLabel, xpLabel, waveLabel, timerLabel;
    private Timeline enemySpawnTimeline, waveTimeline;
    private GameController gameController; // Reference to the GameController

    public GameStateManager(GameController gameController) {
        this.gameController = gameController;
        setupTimelines();
    }

    private void setupTimelines() {
        enemySpawnTimeline = new Timeline(
                new KeyFrame(Duration.seconds(((double) spawnInterval / currentWave)),
                        event -> {
                            gameController.getEnemyAIController().spawnEnemy();
                            // ... any other logic you want here ...
                        })
        );
        enemySpawnTimeline.setCycleCount(Timeline.INDEFINITE);

        waveTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1),
                        event -> {
                            timeRemainingInWave--;
                            gameController.updateTimerLabel();
                            if (timeRemainingInWave <= 0) {
                                endWave();
                            }
                        })
        );
        waveTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void startWave() {
        timeRemainingInWave = WAVE_DURATION;
        gameController.updateTimerLabel();
        enemySpawnTimeline.playFromStart();

        waveTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    timeRemainingInWave--;
                    gameController.updateTimerLabel();
                    if (timeRemainingInWave <= 0) {
                        endWave();
                    }
                })
        );
        waveTimeline.setCycleCount(Timeline.INDEFINITE);
        waveTimeline.play();

        gameController.updateWaveLabel();
    }

    private void endWave() {
        enemySpawnTimeline.stop();
        waveTimeline.stop();

        // Optional: Add some delay or notification before the next wave starts
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(event -> {
            currentWave++;
            startWave();
        });
        delay.play();
    }

    public void startEnemySpawnTimeline() {
        enemySpawnTimeline.playFromStart();
    }

    public void stopEnemySpawnTimeline() {
        enemySpawnTimeline.stop();
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getTimeRemainingInWave() {
        return timeRemainingInWave;
    }

    public double getSpawnInterval() {
        return spawnInterval;
    }

    public void setCurrentWave(int currentWave) {
        this.currentWave = currentWave;
    }

    public void setTimeRemainingInWave(int timeRemainingInWave) {
        this.timeRemainingInWave = timeRemainingInWave;
    }

    public void setSpawnInterval(double spawnInterval) {
        this.spawnInterval = spawnInterval;
    }
}
