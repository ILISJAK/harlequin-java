package com.example.harlequin;

import javafx.event.Event;
import javafx.event.EventType;

public class EnemyCollisionEvent extends Event {
    public static final EventType<EnemyCollisionEvent> ENEMY_COLLIDED = new EventType<>(ANY, "ENEMY_COLLIDED");
    private final double damageDealt;

    public EnemyCollisionEvent(double damageDealt) {
        super(ENEMY_COLLIDED);
        this.damageDealt = damageDealt;
    }

    public double getDamageDealt() {
        return damageDealt;
    }
}
