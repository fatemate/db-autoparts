package ru.edu.nsu.fit.martynov.autoparts.db;

public class ChoiceHelper {
    private int id;
    private String name;

    public ChoiceHelper(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
