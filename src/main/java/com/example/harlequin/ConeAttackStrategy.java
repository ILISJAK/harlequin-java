package com.example.harlequin;

import javafx.animation.PauseTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConeAttackStrategy implements AttackStrategy {
    private Pane gamePane;  // The main game pane where everything is rendered
    private Player player;  // Reference to the player to get position, direction, etc.
    private double CONE_RADIUS;  // Example radius for the cone
    private double CONE_ANGLE;    // Angle of the cone
    private EnemyAIController enemyAIController;

    public ConeAttackStrategy(Pane gamePane, Player player, EnemyAIController enemyAIController) {
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
        double dy = player.getY() - enemyCircle.getTranslateY();

        // Calculate the angle to the closest enemy
        double angleToEnemy = Math.toDegrees(Math.atan2(dy, dx));

        Arc coneAttack = new Arc(
                player.getX(),
                player.getY(),
                CONE_RADIUS,
                CONE_RADIUS,
                angleToEnemy - (CONE_ANGLE / 2),  // Start angle
                CONE_ANGLE                        // Length of the arc angle
        );

        coneAttack.setFill(Color.TRANSPARENT);  // Transparent fill
        coneAttack.setStroke(Color.YELLOW);     // Example color for the outline of the cone
        coneAttack.setStrokeWidth(3);           // Example stroke width
        coneAttack.setType(ArcType.ROUND);      // Round type for the cone

        gamePane.getChildren().add(coneAttack);

        // Remove the visual after some time using PauseTransition
        PauseTransition pause = new PauseTransition(Duration.millis(500));
        pause.setOnFinished(event -> gamePane.getChildren().remove(coneAttack));
        pause.play();

        for (Map.Entry<Enemy, Circle> entry : enemyAIController.getEnemies().entrySet()) {
            if (isWithinCone(entry.getValue())) {
                enemyAIController.damageEnemy(entry.getKey(), player.getAttackPower());
            }
        }
        List<Enemy> enemiesToRemove = new ArrayList<>();
        for (Map.Entry<Enemy, Circle> entry : enemyAIController.getEnemies().entrySet()) {
            if (isWithinCone(entry.getValue())) {
                enemyAIController.damageEnemy(entry.getKey(), player.getAttackPower());
                if (entry.getKey().getHealth() <= 0) {
                    enemiesToRemove.add(entry.getKey());
                }
            }
        }

        for (Enemy enemy : enemiesToRemove) {
            enemyAIController.getEnemies().remove(enemy);
            ((Pane) player.getSprite().getParent()).getChildren().remove(enemy.getSprite());
        }
    }
    private boolean isWithinCone(Circle enemyCircle) {
        double dx = enemyCircle.getTranslateX() - player.getX();
        double dy = enemyCircle.getTranslateY() - player.getY();

        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > CONE_RADIUS) {
            return false; // Enemy is outside the cone's radius
        }

        double angle = Math.toDegrees(Math.atan2(dy, dx));
        // Normalize angle to [0, 360)
        if (angle < 0) {
            angle += 360;
        }

        // Calculate cone's start and end angles based on the closest enemy's angle
        double closestEnemyAngle = Math.toDegrees(Math.atan2(dy, dx));
        double halfCone = CONE_ANGLE / 2.0;
        double startAngle = closestEnemyAngle - halfCone;
        double endAngle = closestEnemyAngle + halfCone;

        // Adjust for the cases where the angle crosses over the 0 or 360 degree mark
        if (startAngle < 0) {
            startAngle += 360;
        }
        if (endAngle > 360) {
            endAngle -= 360;
        }

        // Check if the enemy's angle is within the cone's range
        if ((startAngle < endAngle && angle >= startAngle && angle <= endAngle) ||
                (startAngle > endAngle && (angle >= startAngle || angle <= endAngle))) {
            return true;
        }

        return false;
    }

    @Override
    public boolean canAttack() {
        // This can now return true since the Weapon class is handling the cooldown
        return true;
    }

    @Override
    public void resetCooldown() {
        // This method can remain empty since the Weapon class is handling the cooldown
    }

    public double getCONE_ANGLE() {
        return CONE_ANGLE;
    }

    public double getCONE_RADIUS() {
        return CONE_RADIUS;
    }

    public void setCONE_ANGLE(double CONE_ANGLE) {
        this.CONE_ANGLE = CONE_ANGLE;
    }

    public void setCONE_RADIUS(double CONE_RADIUS) {
        this.CONE_RADIUS = CONE_RADIUS;
    }
}
