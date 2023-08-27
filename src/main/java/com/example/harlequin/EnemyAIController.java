package com.example.harlequin;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class EnemyAIController implements EnemyProvider {
    private final GameController gameController;
    private final Player player;
    private final Map<Enemy, Circle> enemyMap;
    private final Pane gamePane;
    private final int currentWave;
    private final double screenWidth;
    private final double screenHeight;
    private static final long ATTACK_COOLDOWN_DURATION = 500;  // half a second
    private boolean isAttackCooldown = false;
    private Timer attackCooldownTimer = new Timer();

    public EnemyAIController(Player player, Map<Enemy, Circle> enemyMap, Pane gamePane, int currentWave, double screenWidth, double screenHeight, GameController gameController) {
        this.player = player;
        this.enemyMap = enemyMap;
        this.gamePane = gamePane;
        this.currentWave = currentWave;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.gameController = gameController;
    }

    public void moveEnemies() {
        for (Map.Entry<Enemy, Circle> entry : enemyMap.entrySet()) {
            moveEnemyTowardsPlayer(entry.getKey(), entry.getValue());
        }
    }
    public void spawnEnemy() {
        int baseHealth = 30;
        int baseAttack = 25;
        int baseDefense = 1;
        double baseSpeed = 1.5;
        double baseRadius = 50;

        int k = 5;  // adjust this value to change the scaling
        int health = (int) (baseHealth + (gameController.getGameStateManager().getCurrentWave()*k));
        int attack = (int) (baseAttack + (gameController.getGameStateManager().getCurrentWave()*k));
        int defense = (int) (baseDefense + (gameController.getGameStateManager().getCurrentWave()*k));
        double speed = baseSpeed;
        double radius = baseRadius;

        Enemy enemy = new Enemy("Circle", 20, health, attack, defense, speed, radius, 0.1,1.2);
        double speedVariation = 0.2;  // 20% variation
        enemy.setMovementSpeed(enemy.getMovementSpeed() * (1 + Math.random() * speedVariation - speedVariation / 2));

        Circle enemyCircle = enemy.getSprite();  // Example size
        enemyCircle.setFill(Color.RED);  // Making enemy red for distinction
        double margin = 60;
        enemyCircle.setTranslateX(margin + Math.random() * (screenWidth - 2 * margin));
        enemyCircle.setTranslateY(margin + Math.random() * (screenHeight - 2 * margin));

        enemyMap.put(enemy, enemyCircle);

        ((Pane) player.getSprite().getParent()).getChildren().add(enemyCircle);
        System.out.println("Spawned enemy at position: " + enemyCircle.getTranslateX() + ", " + enemyCircle.getTranslateY());
    }

    private void moveEnemyTowardsPlayer(Enemy enemy, Circle enemyCircle) {
        double speed = enemy.getMovementSpeed();
        double margin = 60;

        double dx = player.getSprite().getTranslateX() - enemyCircle.getTranslateX();
        double dy = player.getSprite().getTranslateY() - enemyCircle.getTranslateY();

        double distance = Math.sqrt(dx * dx + dy * dy);

        // Normalize the direction vector (dx, dy)
        double moveX = speed * (dx / distance);
        double moveY = speed * (dy / distance);

        // Add a little randomness to the direction
        double jitter = 0.1;
        moveX += (Math.random() - 0.5) * jitter * speed;
        moveY += (Math.random() - 0.5) * jitter * speed;

        // Check the potential new position
        double potentialNewX = enemyCircle.getTranslateX() + moveX;
        double potentialNewY = enemyCircle.getTranslateY() + moveY;

        System.out.println("Old position: (" + enemyCircle.getTranslateX() + ", " + enemyCircle.getTranslateY() + ")");
        System.out.println("Move amounts: (" + moveX + ", " + moveY + ")");
        System.out.println("New position: (" + (enemyCircle.getTranslateX() + moveX) + ", " + (enemyCircle.getTranslateY() + moveY) + ")");

        // Check bounds for X position
        if (potentialNewX - enemyCircle.getRadius() >= margin && potentialNewX + enemyCircle.getRadius() <= screenWidth - margin) {
            enemyCircle.setTranslateX(potentialNewX);
        }

        // Check bounds for Y position
        if (potentialNewY - enemyCircle.getRadius() >= margin && potentialNewY + enemyCircle.getRadius() <= screenHeight - margin) {
            enemyCircle.setTranslateY(potentialNewY);
        }

        // If the enemy is too close to an edge, reposition it slightly inward
        if (enemyCircle.getTranslateX() - enemyCircle.getRadius() < margin) {
            enemyCircle.setTranslateX(margin + enemyCircle.getRadius());
        }
        if (enemyCircle.getTranslateX() + enemyCircle.getRadius() > screenWidth - margin) {
            enemyCircle.setTranslateX(screenWidth - margin - enemyCircle.getRadius());
        }
        if (enemyCircle.getTranslateY() - enemyCircle.getRadius() < margin) {
            enemyCircle.setTranslateY(margin + enemyCircle.getRadius());
        }
        if (enemyCircle.getTranslateY() + enemyCircle.getRadius() > screenHeight - margin) {
            enemyCircle.setTranslateY(screenHeight - margin - enemyCircle.getRadius());
        }

        // Adjust movement to slide along the edge if near the edge
        if (enemyCircle.getTranslateX() - enemyCircle.getRadius() <= margin || enemyCircle.getTranslateX() + enemyCircle.getRadius() >= screenWidth - margin) {
            if (player.getSprite().getTranslateY() > enemyCircle.getTranslateY()) {
                moveY = speed;
            } else {
                moveY = -speed;
            }
            moveX = 0;
        }
        if (enemyCircle.getTranslateY() - enemyCircle.getRadius() <= margin || enemyCircle.getTranslateY() + enemyCircle.getRadius() >= screenHeight - margin) {
            if (player.getSprite().getTranslateX() > enemyCircle.getTranslateX()) {
                moveX = speed;
            } else {
                moveX = -speed;
            }
            moveY = 0;
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
        if (areCirclesColliding(enemyCircle, player.getSprite()) && !isAttackCooldown) {
            System.out.println("Enemy and player are colliding.");
            System.out.println("Attack Cooldown Status: " + isAttackCooldown);
            double damageDealt = enemy.getDamage(player);
            EnemyCollisionEvent event = new EnemyCollisionEvent(damageDealt);
            System.out.println("Event source: " + event.getSource());
            System.out.println("Event target: " + event.getTarget());
            System.out.println("Firing the EnemyCollisionEvent with damage: " + damageDealt);
            System.out.println("Player health: " + player.getHealth());
            gameController.getGamePane().fireEvent(event);
            // Move enemy back
            enemyCircle.setTranslateX(enemyCircle.getTranslateX() - moveX);
            enemyCircle.setTranslateY(enemyCircle.getTranslateY() - moveY);
        }

        if (moveX > 0 && moveY > 0) {
            enemyCircle.setFill(Color.GREEN); // moving towards the player
        } else if (moveX < 0 || moveY < 0) {
            enemyCircle.setFill(Color.RED); // moving away from the player
        }
    }

    // helper methods
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
    private boolean areCirclesColliding(Circle c1, Circle c2) {
        double dx = c1.getTranslateX() - c2.getTranslateX();
        double dy = c1.getTranslateY() - c2.getTranslateY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double minDistance = c1.getRadius() + c2.getRadius();

        return distance < minDistance;
    }
    public void startAttackCooldown() {
        isAttackCooldown = true;
        attackCooldownTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isAttackCooldown = false;
            }
        }, ATTACK_COOLDOWN_DURATION);
    }

    @Override
    public Map<Enemy, Circle> getEnemies(){ return enemyMap;}
    @Override
    public Circle getEnemyCircle(Enemy enemy) {
        return enemyMap.get(enemy);
    }
    @Override
    public void damageEnemy(Enemy enemy, double damageAmount) {
        enemy.takeDamage(damageAmount);
        enemy.getSprite().setBlendMode(BlendMode.DIFFERENCE);
        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(event -> enemy.getSprite().setBlendMode(null)); // Reset the blend mode after a short duration
        pause.play();

        // Knockback effect
        double knockbackDistance = 100;  // This can be adjusted as needed
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
            player.gainExperience(enemy.getExperienceValue());
            gameController.updatePlayerState();

        }
    }
}
