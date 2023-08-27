package com.example.harlequin;

import javafx.scene.shape.Circle;

import java.util.Map;

public interface EnemyProvider {
    Map<Enemy, Circle> getEnemies();
    Enemy getClosestEnemy();
    Circle getEnemyCircle(Enemy enemy);
    void damageEnemy(Enemy enemy, double damageAmount);
}
