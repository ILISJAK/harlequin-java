package com.example.harlequin;

public class Enemy extends Character {
    private int experienceValue;
    public Enemy(String name, int level, int health, int attackPower, int defense, double movementSpeed, double radius){
        super(name, level, health, attackPower, defense, movementSpeed, radius);

        setExperienceValue(getLevel()*3);
    }
    public int getExperienceValue() {
        return experienceValue;
    }
    public void setExperienceValue(int experienceValue) {
        this.experienceValue = experienceValue;
    }
}
