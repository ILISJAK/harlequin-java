package com.example.harlequin;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class ProjectileAttackStrategy implements AttackStrategy {
    private Pane gamePane;
    private Player player;
    private EnemyAIController enemyAIController;
    private static final double PROJECTILE_RADIUS = 10;  // Example radius for the projectile
    private static final double PROJECTILE_SPEED = 10;  // Speed at which projectile travels

    public ProjectileAttackStrategy(Pane gamePane, Player player, EnemyAIController enemyAIController) {
        if (gamePane == null) {
            throw new IllegalArgumentException("Game pane cannot be null.");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }
        if (enemyAIController == null) {
            throw new IllegalArgumentException("enemyAIController cannot be null.");
        }
        this.gamePane = gamePane;
        this.player = player;
        this.enemyAIController = enemyAIController;
    }

    @Override
    public void executeAttack() {
        Enemy closestEnemy = enemyAIController.getClosestEnemy();
        if (closestEnemy == null) {
            return;  // No enemies, so don't execute the attack
        }

        Circle enemyCircle = enemyAIController.getEnemyCircle(closestEnemy);

        double dx = enemyCircle.getTranslateX() - player.getX();
        double dy = enemyCircle.getTranslateY() - player.getY();

        double distance = Math.sqrt(dx * dx + dy * dy);
        double normalizedDx = dx / distance;
        double normalizedDy = dy / distance;

        // Calculate the position above the player's head
        double offsetX = normalizedDx * PROJECTILE_RADIUS;
        double offsetY = normalizedDy * PROJECTILE_RADIUS;
        double projectileX = player.getX() + offsetX * 2;
        double projectileY = player.getY() + offsetY * 2;

        Circle projectile = new Circle(projectileX, projectileY, PROJECTILE_RADIUS, Color.BLUE);

        gamePane.getChildren().add(projectile);

        // Create an animation for the projectile
        final Timeline projectileAnimation = new Timeline(); // Initialize here

        projectileAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(16), event -> {  // 60 frames per second
                    projectile.setTranslateX(projectile.getTranslateX() + normalizedDx * PROJECTILE_SPEED);
                    projectile.setTranslateY(projectile.getTranslateY() + normalizedDy * PROJECTILE_SPEED);

                    // Check collision with enemies
                    Map<Enemy, Circle> enemiesCopy = new HashMap<>(enemyAIController.getEnemies());
                    for (Map.Entry<Enemy, Circle> entry : enemiesCopy.entrySet()) {
                        if (projectile.getBoundsInParent().intersects(entry.getValue().getBoundsInParent())) {
                            enemyAIController.damageEnemy(entry.getKey(), player.getAttackPower());
                            gamePane.getChildren().remove(projectile);
                            projectileAnimation.stop();
                        }
                    }

                    // Remove projectile if it goes out of bounds
                    if (projectile.getTranslateX() < 0 || projectile.getTranslateX() > gamePane.getWidth() ||
                            projectile.getTranslateY() < 0 || projectile.getTranslateY() > gamePane.getHeight()) {
                        gamePane.getChildren().remove(projectile);
                        projectileAnimation.stop();
                    }
                })
        );

        projectileAnimation.setCycleCount(Timeline.INDEFINITE);
        projectileAnimation.play();
    }

    @Override
    public boolean canAttack() {
        return true;
    }

    @Override
    public void resetCooldown() {
        // This method can remain empty since we are not handling the cooldown here
    }
}
