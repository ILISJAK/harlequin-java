package com.example.harlequin;

public class Weapon extends Item {
    private final AttackStrategy attackStrategy;
    private static final long COOLDOWN_DURATION = 1000;  // 1 second for example, adjust as needed
    private long lastAttackTime = 0;  // Time of the last attack
    private final int damage;  // The damage the weapon can deal

    public Weapon(String name, String description, int damage, AttackStrategy attackStrategy) {
        super(name, description);
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

    // ... Additional getters/setters or methods as required ...
}
