package com.easierlifeapps.easypacking.Adapters;

public class SingleListAplikator {
    private String name, notes, category, color;
    private int packed, pcs;

    public SingleListAplikator(String name, String notes, String category, int packed, int pcs, String color) {
        this.name = name;
        this.notes = notes;
        this.category = category;
        this.packed = packed;
        this.pcs = pcs;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPacked() {
        return packed;
    }

    public void setPacked(int packed) {
        this.packed = packed;
    }

    public int getPcs() {
        return pcs;
    }

    public void setPcs(int pcs) {
        this.pcs = pcs;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
