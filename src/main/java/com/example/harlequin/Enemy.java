package com.example.harlequin;

import javafx.scene.image.Image;

public class Enemy extends Character {
    private Image enemyMoveLeftAnimation, enemyMoveRightAnimation;
    private final int frameCount;
    private final int frameWidth;
    private final int frameHeight;
    private int experienceValue;

    public Enemy(String name, int level, int health, int attackPower, int defense, double movementSpeed, double radius, double critChance, double critMultiplier, Image enemyMoveLeftAnimation, Image enemyMoveRightAnimation, int frameCount, int frameWidth, int frameHeight){
        super(name, level, health, attackPower, defense, movementSpeed, radius, critChance, critMultiplier);

        setExperienceValue(getLevel()*3);
        this.enemyMoveLeftAnimation = enemyMoveLeftAnimation;
        this.enemyMoveRightAnimation = enemyMoveRightAnimation;
        this.frameCount = frameCount;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    public Enemy(String name, int level, int health, int attackPower, int defense, double movementSpeed, double radius, double critChance, double critMultiplier, Image enemyMoveLeftAnimation, Image enemyMoveRightAnimation){
        this(name, level, health, attackPower, defense, movementSpeed, radius, critChance, critMultiplier, enemyMoveLeftAnimation, enemyMoveRightAnimation, 6, 288, 48);
    }

    public int getExperienceValue() {
        return experienceValue;
    }
    public void setExperienceValue(int experienceValue) {
        this.experienceValue = experienceValue;
    }

    public Image getEnemyMoveLeftAnimation() {
        return enemyMoveLeftAnimation;
    }
    public Image getEnemyMoveRightAnimation() {
        return enemyMoveRightAnimation;
    }
    public void setEnemyMoveLeftAnimation(Image enemyMoveLeftAnimation) {
        this.enemyMoveLeftAnimation = enemyMoveLeftAnimation;
    }
    public void setEnemyMoveRightAnimation(Image enemyMoveRightAnimation) {
        this.enemyMoveRightAnimation = enemyMoveRightAnimation;
    }
    public int getFrameCount() {
        return frameCount;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }
}
