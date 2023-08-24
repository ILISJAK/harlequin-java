package com.example.harlequin;

import javafx.scene.canvas.GraphicsContext;

import java.util.List;

public interface AttackStrategy {
    void executeAttack();
    boolean canAttack(); // Check if the weapon can attack based on its cooldown.
    void resetCooldown(); // Reset the cooldown after an attack.
}

