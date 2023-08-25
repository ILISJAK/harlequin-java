package com.example.harlequin;

import com.almasb.fxgl.scene3d.Cone;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;

import java.util.*;

import javafx.scene.control.Label;
import javafx.util.Duration;

public class GameController {
    @FXML
    private Pane gamePane;
    @FXML
    private Rectangle healthBar, healthBarBackground;
    @FXML
    private Label playerLevelLabel, xpLabel, waveLabel, timerLabel;
    private Player player;
    private Timeline enemySpawnTimeline, waveTimeline;
    private EnemyAIController enemyAIController;

    private int currentWave = 1;
    private int timeRemainingInWave;
    private static final int WAVE_DURATION = 10;
    private static final double INITIAL_SPAWN_INTERVAL = 1;
    private double spawnInterval = INITIAL_SPAWN_INTERVAL;

    private static final double SCREEN_WIDTH = Screen.getPrimary().getBounds().getWidth();
    private static final double SCREEN_HEIGHT = Screen.getPrimary().getBounds().getHeight();
    private static final double MARGIN = 60;

    private Map<Enemy, Circle> enemyMap = new HashMap<>();
    private final Set<KeyCode> activeKeys = new HashSet<>();


    private double screenWidth = Screen.getPrimary().getBounds().getWidth();
    private double screenHeight = Screen.getPrimary().getBounds().getHeight();
    private final AnimationTimer gameLoop = new AnimationTimer() {
        @Override
        public void handle(long now) {
            System.out.println("Game loop running...");

            double speed = player.getMovementSpeed();
            System.out.println("Player speed: " + speed);
            if (activeKeys.contains(KeyCode.W) && player.getSprite().getTranslateY() - speed - player.getSprite().getRadius() > 0) {
                player.getSprite().setTranslateY(player.getSprite().getTranslateY() - speed);
            }
            if (activeKeys.contains(KeyCode.S) && player.getSprite().getTranslateY() + speed + player.getSprite().getRadius() < 1080) {
                player.getSprite().setTranslateY(player.getSprite().getTranslateY() + speed);
            }
            if (activeKeys.contains(KeyCode.A) && player.getSprite().getTranslateX() - speed - player.getSprite().getRadius() > 0) {
                player.getSprite().setTranslateX(player.getSprite().getTranslateX() - speed);
            }
            if (activeKeys.contains(KeyCode.D) && player.getSprite().getTranslateX() + speed + player.getSprite().getRadius() < 1920) {
                player.getSprite().setTranslateX(player.getSprite().getTranslateX() + speed);
            }
            System.out.println("Number of enemies: " + enemyMap.size());
            enemyAIController.moveEnemies();
            player.attack();
        }
    };
    private void startWave() {
        timeRemainingInWave = WAVE_DURATION;
        updateTimerLabel();
        enemySpawnTimeline.playFromStart();

        waveTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    timeRemainingInWave--;
                    updateTimerLabel();
                    if (timeRemainingInWave <= 0) {
                        endWave();
                    }
                })
        );
        waveTimeline.setCycleCount(Timeline.INDEFINITE);
        waveTimeline.play();

        updateWaveLabel();
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
    private void updateWaveLabel() {
        waveLabel.setText("Wave: " + currentWave);
    }

    private void updateTimerLabel() {
        timerLabel.setText("Time Remaining: " + timeRemainingInWave + "s");
    }
    private void updateHealthBar() {
        double percentageHealth = (double) player.getHealth() / player.getMaxHealth();
        healthBar.setWidth(200 * percentageHealth);  // Assuming the max width of the health bar is 200
        if (percentageHealth < 0.3) {
            healthBar.setFill(Color.RED); // Health bar turns red when health is below 30%
        } else if (percentageHealth < 0.6) {
            healthBar.setFill(Color.ORANGE); // Health bar turns orange when health is below 60%
        } else {
            healthBar.setFill(Color.GREEN);
        }
    }
    private void displayDamage(int damage) {
        Label damageLabel = new Label("-" + String.valueOf(damage));
        damageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 24;");
        damageLabel.setTranslateX(player.getSprite().getTranslateX());
        damageLabel.setTranslateY(player.getSprite().getTranslateY() - 20); // Slightly above the player

        // Add to scene
        ((Pane) player.getSprite().getParent()).getChildren().add(damageLabel);

        // Animate the damage label
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1500), damageLabel);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(event -> {
            // Remove label from scene
            ((Pane) player.getSprite().getParent()).getChildren().remove(damageLabel);
        });
        fadeTransition.play();
    }
    private void updatePlayerLevelLabel() {
        playerLevelLabel.setText("Player Level: " + player.getLevel());
    }
    private void checkGameOver() {
        if (player.getHealth() <= 0) {
            gameLoop.stop();
            enemySpawnTimeline.stop();

            Label gameOverLabel = new Label("GAME OVER");
            gameOverLabel.setStyle("-fx-text-fill: red; -fx-font-size: 48;");
            gameOverLabel.setLayoutX(screenWidth / 2);  // Adjust to center the label
            gameOverLabel.setLayoutY(screenHeight / 2);  // Adjust to center the label

            ((Pane) player.getSprite().getParent()).getChildren().add(gameOverLabel);
            gamePane.requestLayout();
        }
    }
    private void updateXpLabel(){
        xpLabel.setText("XP: " + player.getExperience() + "/" + player.getXpNeeded());
    }
    public void updatePlayerState(){
        updateXpLabel();
        updatePlayerLevelLabel();
    }
    public void initialize() {
        player = new Player("Hero", 1, 250, 15, 5, 2, 50);
        ((Pane) healthBar.getParent()).getChildren().add(player.getSprite());
        player.getSprite().setTranslateX(960.0); // Half of 1920
        player.getSprite().setTranslateY(540.0); // Half of 1080
        updatePlayerState();

        enemyAIController = new EnemyAIController(player, enemyMap, (Pane) player.getSprite().getParent(), currentWave, screenWidth, screenHeight, this);

        Weapon coneWeapon = new Weapon("ConeTest", "Conetest", 10, new ConeAttackStrategy((Pane) player.getSprite().getParent(), player, enemyAIController));
        Weapon projectileWeapon = new Weapon("ProjectileTest", "ProjectileTest", 10, new ProjectileAttackStrategy((Pane) player.getSprite().getParent(), player, enemyAIController));
        projectileWeapon.setCOOLDOWN_DURATION(50);
        player.setWeapons(Arrays.asList(coneWeapon, projectileWeapon));

        System.out.println("After Initialization: ");
        System.out.println("Player object: " + player);
        System.out.println("EnemyAIController object: " + enemyAIController);

        // Delay the setup of the event handler
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            gamePane.addEventHandler(EnemyCollisionEvent.ENEMY_COLLIDED, collisionEvent -> {
                System.out.println("Player object: " + player);
                System.out.println("EnemyAIController object: " + enemyAIController);
                if (player != null) {
                    player.takeDamage(collisionEvent.getDamageDealt());
                    updateHealthBar();
                    displayDamage(collisionEvent.getDamageDealt());
                }
                if (enemyAIController != null) {
                    enemyAIController.startAttackCooldown();
                }
                checkGameOver();
            });
        });
        delay.play();

        gameLoop.start();

        enemySpawnTimeline = new Timeline(new KeyFrame(Duration.seconds(((double) spawnInterval / currentWave)), event -> enemyAIController.spawnEnemy())); // Spawns an enemy every SPAWN_INTERVAL seconds
        enemySpawnTimeline.setCycleCount(Timeline.INDEFINITE); // Keeps looping forever

        // Initialize wave related labels and start the first wave
        updateWaveLabel();
        startWave();
    }

    public void handleKeyPressed(KeyEvent event) {
        activeKeys.add(event.getCode());
    }
    public void handleKeyReleased(KeyEvent event) {
        activeKeys.remove(event.getCode());
    }
}
