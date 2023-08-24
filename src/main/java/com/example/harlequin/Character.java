package com.example.harlequin;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public abstract class Character {
    protected String name;
    protected int level;
    protected int health;
    protected int maxHealth;
    protected int attackPower;
    protected int defense;
    protected double movementSpeed;

    protected Circle sprite; // graphical representation

    public Character(String name, int level, int health, int attackPower, int defense, double movementSpeed, double radius) {
        setName(name);
        setLevel(level);
        setMaxHealth(health);
        setHealth(getMaxHealth());
        setAttackPower(attackPower);
        setDefense(defense);
        setMovementSpeed(movementSpeed);

        // Initialize the sprite with the given radius and default color (this can be changed as needed)
        this.sprite = new Circle(radius);
        this.sprite.setFill(Color.BLUE);
    }

    // Get damage to be dealt based on attack and defense values
    public int getDamage(Character opponent) {
        int damage = this.attackPower - opponent.defense;
        return Math.max(damage, 0); // Ensure damage is non-negative
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health < 0) {
            this.health = 0; // Ensure health doesn't go negative
        }
    }
    // setters
    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefense(int defense) {
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
    public int getAttackPower() {
        return attackPower;
    }
    public int getDefense() {
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
}
