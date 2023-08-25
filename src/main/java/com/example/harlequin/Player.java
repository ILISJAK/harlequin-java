package com.example.harlequin;

import java.util.ArrayList;
import java.util.List;

public class Player extends Character {
    private static final int BASE_XP_NEEDED = 100;
    private int xpNeeded = BASE_XP_NEEDED;
    private int experience;
    private List<Weapon> weapons;

    public Player (String name, int level, int health, int attackPower, int defense, double movementSpeed, double radius ) {
        super(name, level, health, attackPower, defense, movementSpeed, radius);
    }

    public void attack() {
        for (Weapon weapon : weapons) {
            if (weapon.canAttack()) {
                weapon.executeAttack();
            }
        }
    }

    public void gainExperience(int xpGained) {
        this.experience += xpGained;
        while (this.experience >= xpNeeded){
            levelUp();
        }
    }

    private void levelUp(){
        this.setLevel(this.getLevel() + 1);
        this.experience -= xpNeeded;
        xpNeeded = BASE_XP_NEEDED * this.getLevel();
    }

    public int getExperience() {
        return experience;
    }

    public int getXpNeeded() {
        return xpNeeded;
    }
    public List<Weapon> getWeapons() {
        return weapons;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void setWeapons(List<Weapon> weapons) {
        this.weapons = weapons;
    }
}
