package com.easierlifeapps.easypacking.Adapters;

public class ListsAplikator {
    private String name;
    private int percentage;

    public ListsAplikator(String name, int percentage) {
        this.name = name;
        this.percentage = percentage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
}
