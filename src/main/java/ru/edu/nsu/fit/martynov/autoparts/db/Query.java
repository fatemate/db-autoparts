package ru.edu.nsu.fit.martynov.autoparts.db;

public class Query {
    private String title;
    private String sql;

    public Query(String title, String sql) {
        this.title = title;
        this.sql = sql;
    }

    public String getQuery() {
        return sql;
    }

    @Override
    public String toString() {
        return title;
    }
}
