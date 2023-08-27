package com.example.harlequin;

import javafx.scene.image.Image;

public class Weapon extends Item {
    private final AttackStrategy attackStrategy;
    private long COOLDOWN_DURATION = 1000;  // 1 second for example, adjust as needed
    private long lastAttackTime = 0;  // Time of the last attack
    private final int damage;  // The damage the weapon can deal

    public Weapon(String name, String description, int damage, AttackStrategy attackStrategy, Runnable effect, int maxLevel, Image image) {
        super(name, description, effect, maxLevel, image);
        this.damage = damage;
        this.attackStrategy = attackStrategy;
    }

    /**
     * Executes the attack using the weapon's strategy.
     * This method checks the cooldown and if the weapon is ready,
     * it performs the attack and updates the last attack time.
     */
    public void executeAttack() {
        if (canAttack()) {
            attackStrategy.executeAttack();
            lastAttackTime = System.currentTimeMillis();  // Record the time of the attack
        }
    }

    /**
     * Checks if the weapon is ready to attack based on its cooldown.
     *
     * @return true if the weapon can attack, false otherwise.
     */
    public boolean canAttack() {
        return (System.currentTimeMillis() - lastAttackTime) > COOLDOWN_DURATION;
    }

    /**
     * Gets the damage value of the weapon.
     *
     * @return the damage the weapon can deal.
     */
    public int getDamage() {
        return damage;
    }

    public long getCOOLDOWN_DURATION() {
        return COOLDOWN_DURATION;
    }

    public void setCOOLDOWN_DURATION(long COOLDOWN_DURATION) {
        this.COOLDOWN_DURATION = COOLDOWN_DURATION;
    }
}
