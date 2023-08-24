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

public class GameController implements EnemyProvider {

    @FXML
    private Rectangle healthBar, healthBarBackground;
    @FXML
    private Label playerLevelLabel;
    private Player player;
    private Timeline enemySpawnTimeline;

    private double screenWidth = Screen.getPrimary().getBounds().getWidth();
    private double screenHeight = Screen.getPrimary().getBounds().getHeight();

    private Map<Enemy, Circle> enemyMap = new HashMap<>();

    private final Set<KeyCode> activeKeys = new HashSet<>();

    // helper method - collision detection
    private boolean areCirclesColliding(Circle c1, Circle c2) {
        double dx = c1.getTranslateX() - c2.getTranslateX();
        double dy = c1.getTranslateY() - c2.getTranslateY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double minDistance = c1.getRadius() + c2.getRadius();

        return distance < minDistance;
    }

    private boolean isAttackCooldown = false;
    private Timer attackCooldownTimer = new Timer();
    private static final long ATTACK_COOLDOWN_DURATION = 500;  // half a second
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

            for (Map.Entry<Enemy, Circle> entry : enemyMap.entrySet()) {
                moveEnemyTowardsPlayer(entry.getKey(),entry.getValue());
            }
            player.attack();
        }
    };

    private void spawnEnemy() {
        Enemy enemy = new Enemy("Circle", 50, 30, 25, 1, 1.5, 50);

        Circle enemyCircle = enemy.getSprite();  // Example size
        enemyCircle.setFill(Color.RED);  // Making enemy red for distinction
        enemyCircle.setTranslateX(Math.random() * 600);  // Random x position
        enemyCircle.setTranslateY(Math.random() * 400);  // Random y position

        enemyMap.put(enemy, enemyCircle);

        ((Pane) player.getSprite().getParent()).getChildren().add(enemyCircle);
        System.out.println("Spawned enemy at position: " + enemyCircle.getTranslateX() + ", " + enemyCircle.getTranslateY());
    }
    private void moveEnemyTowardsPlayer(Enemy enemy, Circle enemyCircle) {
        double speed = enemy.getMovementSpeed();

        double dx = player.getSprite().getTranslateX() - enemyCircle.getTranslateX();
        double dy = player.getSprite().getTranslateY() - enemyCircle.getTranslateY();

        double distance = Math.sqrt(dx * dx + dy * dy);

        // Normalize the direction vector (dx, dy)
        double moveX = speed * (dx / distance);
        double moveY = speed * (dy / distance);

        // Check the potential new position
        double potentialNewX = enemyCircle.getTranslateX() + moveX;
        double potentialNewY = enemyCircle.getTranslateY() + moveY;

        // Check bounds for X position
        if (potentialNewX - enemyCircle.getRadius() >= 0 && potentialNewX + enemyCircle.getRadius() <= screenWidth) {
            enemyCircle.setTranslateX(potentialNewX);
        }

        // Check bounds for Y position
        if (potentialNewY - enemyCircle.getRadius() >= 0 && potentialNewY + enemyCircle.getRadius() <= screenHeight) {
            enemyCircle.setTranslateY(potentialNewY);
        }

        for (Circle otherEnemyCircle : enemyMap.values()) {
            if (otherEnemyCircle != enemyCircle && areCirclesColliding(enemyCircle, otherEnemyCircle)) {
                // Move the enemy away from the other enemy it collided with
                double collisionDx = enemyCircle.getTranslateX() - otherEnemyCircle.getTranslateX();
                double collisionDy = enemyCircle.getTranslateY() - otherEnemyCircle.getTranslateY();
                double collisionDistance = Math.sqrt(collisionDx * collisionDx + collisionDy * collisionDy);

                double repelX = speed * (collisionDx / collisionDistance);
                double repelY = speed * (collisionDy / collisionDistance);

                enemyCircle.setTranslateX(enemyCircle.getTranslateX() + repelX);
                enemyCircle.setTranslateY(enemyCircle.getTranslateY() + repelY);
            }
        }
        // Collision check with player
        if (areCirclesColliding(enemyCircle, player.getSprite())) {
            if (!isAttackCooldown) {
                int damageDealt = enemy.getDamage(player);
                player.takeDamage(damageDealt);
                updateHealthBar();
                displayDamage(damageDealt);
                startAttackCooldown();
                checkGameOver();
            }
            // Move enemy back
            enemyCircle.setTranslateX(enemyCircle.getTranslateX() - moveX);
            enemyCircle.setTranslateY(enemyCircle.getTranslateY() - moveY);
        }
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

    private void startAttackCooldown() {
        isAttackCooldown = true;
        attackCooldownTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isAttackCooldown = false;
            }
        }, ATTACK_COOLDOWN_DURATION);
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
    public Enemy getClosestEnemy() {
        double minDistance = Double.MAX_VALUE;
        Enemy closestEnemy = null;

        for (Map.Entry<Enemy, Circle> entry : enemyMap.entrySet()) {
            double dx = entry.getValue().getTranslateX() - player.getSprite().getTranslateX();
            double dy = entry.getValue().getTranslateY() - player.getSprite().getTranslateY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < minDistance) {
                minDistance = distance;
                closestEnemy = entry.getKey();
            }
        }

        return closestEnemy;
    }
    @Override
    public Circle getEnemyCircle(Enemy enemy) {
        return enemyMap.get(enemy);
    }
    @Override
    public void damageEnemy(Enemy enemy, int damageAmount) {
        enemy.takeDamage(damageAmount);
        enemy.getSprite().setBlendMode(BlendMode.DIFFERENCE);
        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(event -> enemy.getSprite().setBlendMode(null)); // Reset the blend mode after a short duration
        pause.play();

        // Knockback effect
        double knockbackDistance = 50;  // This can be adjusted as needed
        Circle enemyCircle = enemy.getSprite();
        double dx = enemyCircle.getTranslateX() - player.getSprite().getTranslateX();
        double dy = enemyCircle.getTranslateY() - player.getSprite().getTranslateY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Compute normalized direction
        double moveX = knockbackDistance * (dx / distance);
        double moveY = knockbackDistance * (dy / distance);

        // Apply knockback
        enemyCircle.setTranslateX(enemyCircle.getTranslateX() + moveX);
        enemyCircle.setTranslateY(enemyCircle.getTranslateY() + moveY);

        // Remove enemy if health is zero or below
        if (enemy.getHealth() <= 0) {
            enemyMap.remove(enemy);
            ((Pane) player.getSprite().getParent()).getChildren().remove(enemyCircle);
        }
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
        }
    }

    public void initialize() {
        player = new Player("Hero", 1, 250, 15, 5, 2, 50);
        player.getSprite().setTranslateX(960.0); // Half of 1920
        player.getSprite().setTranslateY(540.0); // Half of 1080
        ((Pane) healthBar.getParent()).getChildren().add(player.getSprite());
        Weapon coneWeapon = new Weapon("ConeTest", "Conetest", 10, new ConeAttackStrategy((Pane) player.getSprite().getParent(), player, this));
        player.setWeapons(Arrays.asList(coneWeapon));
        gameLoop.start();
        spawnEnemy();
        spawnEnemy();
        spawnEnemy();
        enemySpawnTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> spawnEnemy())); // Spawns an enemy every 5 seconds
        enemySpawnTimeline.setCycleCount(Timeline.INDEFINITE); // Keeps looping forever
        enemySpawnTimeline.play();
    }

    public void handleKeyPressed(KeyEvent event) {
        activeKeys.add(event.getCode());
    }
    public void handleKeyReleased(KeyEvent event) {
        activeKeys.remove(event.getCode());
    }
    @Override
    public Map<Enemy, Circle> getEnemies(){ return enemyMap;}
}
