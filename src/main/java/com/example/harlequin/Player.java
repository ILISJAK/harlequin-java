package com.example.harlequin;

import java.util.ArrayList;
import java.util.List;

public class Player extends Character {
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

    public int getExperience() {
        return experience;
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
