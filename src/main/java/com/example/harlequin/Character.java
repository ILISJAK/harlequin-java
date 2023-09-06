package com.example.harlequin;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Random;

public abstract class Character {
    protected String name;
    protected int level;
    protected int health;
    protected int maxHealth;
    protected double attackPower;
    protected double defense;
    protected double movementSpeed;
    protected double critChance; // chance to land a critical hit
    protected double critMultiplier; // multiplier for critical hit damage
    private final Random random = new Random();
    protected boolean lastAttackWasCritical;
    private boolean isMoving = false;
    private boolean isFacingLeft = false;

    protected Circle sprite; // graphical representation

    public Character(String name, int level, int health, double attackPower, double defense, double movementSpeed, double radius, double critChance, double critMultiplier) {
        setName(name);
        setLevel(level);
        setMaxHealth(health);
        setHealth(getMaxHealth());
        setAttackPower(attackPower);
        setDefense(defense);
        setMovementSpeed(movementSpeed);
        setCritChance(critChance);
        setCritMultiplier(critMultiplier);
        // Initialize the sprite with the given radius and default color (this can be changed as needed)
        this.sprite = new Circle(radius);
        this.sprite.setFill(Color.BLUE);
    }

    public double getDamage(Character opponent) {
        // Get a random factor between 0.8 and 1.2 (you can adjust this range as needed)
        double randomFactor = 0.9 + (1.1 - 0.9) * random.nextDouble();

        // Apply the random factor to the attack power
        int adjustedAttackPower = (int) (this.attackPower * randomFactor);

        double baseDamage = adjustedAttackPower - opponent.defense;
        baseDamage = Math.max(baseDamage, 0); // Ensure damage is non-negative

        // Determine if this attack is a critical hit
        boolean isCriticalHit = random.nextDouble() < this.critChance;

        // If it's a critical hit, multiply the damage by the critical hit multiplier
        double finalDamage = isCriticalHit ? (int) (baseDamage * this.critMultiplier) : baseDamage;

        // Update lastAttackWasCritical
        this.lastAttackWasCritical = isCriticalHit;

        return finalDamage;
    }

    public void takeDamage(double damage) {
        System.out.println("Taking damage: " + damage);
        this.health -= damage;
        if (this.health < 0) {
            this.health = 0; // Ensure health doesn't go negative
        }
    }
    public boolean wasLastAttackCritical() {
        return lastAttackWasCritical;
    }

    // setters
    public void setAttackPower(double attackPower) {
        this.attackPower = attackPower;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }

    public void setHealth(int health) {
        this.health = health;
    }
    public String getName() {
        return name;
    }

    public void setMovementSpeed(double movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }
    public void setX(double x) {
        sprite.setTranslateX(x);
    }
    public void setY(double y) {
        sprite.setTranslateY(y);
    }
    public void setCritChance(double critChance) {
        this.critChance = critChance;
    }
    public void setCritMultiplier(double critMultiplier) {
        this.critMultiplier = critMultiplier;
    }
    public void setLastAttackWasCritical(boolean lastAttackWasCritical) {
        this.lastAttackWasCritical = lastAttackWasCritical;
    }
    public void setMoving(boolean moving) {
        isMoving = moving;
    }
    public void setFacingLeft(boolean facingLeft) {
        isFacingLeft = facingLeft;
    }

    // getters
    public Circle getSprite() {
        return sprite;
    }
    public double getX() {
        return sprite.getTranslateX();
    }
    public double getY() {
        return sprite.getTranslateY();
    }
    public double getAttackPower() {
        return attackPower;
    }
    public double getDefense() {
        return defense;
    }
    public int getHealth() {
        return health;
    }
    public double getMovementSpeed() {
        return movementSpeed;
    }
    public int getLevel() {
        return level;
    }
    public int getMaxHealth() {
        return maxHealth;
    }
    public double getCritChance() {
        return critChance;
    }
    public double getCritMultiplier() {
        return critMultiplier;
    }

    public boolean getFacingLeft() {
        return isFacingLeft;
    }

    public boolean getMoving() {
        return isMoving;
    }
}