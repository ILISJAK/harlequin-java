package com.example.harlequin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EnemyDatabase {
    private static EnemyDatabase instance = null;
    private final Map<String, Enemy> enemyTypes;

    private EnemyDatabase() {
        enemyTypes = new HashMap<>();
    }

    public static EnemyDatabase getInstance() {
        if (instance == null) {
            instance = new EnemyDatabase();
        }
        return instance;
    }

    public void addEnemyType(String name, Enemy enemy) {
        enemyTypes.put(name, enemy);
    }

    public Enemy getEnemyType(String name) {
        return enemyTypes.get(name);
    }

    public Set<String> getEnemyTypes() {
        return enemyTypes.keySet();
    }
}
