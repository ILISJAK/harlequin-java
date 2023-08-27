package com.example.harlequin;

import javafx.scene.image.Image;

public class Item implements Upgrade {
    private String name;
    private String description;
    private Runnable effect;
    private int level;
    private int maxLevel;
    private Image image;
    public Item(String name, String description, Runnable effect, int maxLevel, Image image) {
        this.name = name;
        this.description = description;
        this.effect = effect;
        this.level = 1;
        this.maxLevel = maxLevel;
        this.image = image;

        if (image.isError()) {
            throw new IllegalArgumentException("Invalid image path");
        }
    }

    public void upgrade() {
        if (level < maxLevel) {
            level++;
            apply();
        }
    }

    public void apply() {
        if (effect != null) {
            effect.run();
        }
    }

    // getters
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }
    public int getMaxLevel() {
        return maxLevel;
    }
    public Image getImage() {
        return image;
    }

    // setters
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
    public void setImage(Image image) {
        this.image = image;
    }
}

