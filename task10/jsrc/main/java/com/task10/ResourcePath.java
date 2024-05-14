package com.task10;

public enum ResourcePath {
    SIGN_UP("/signup"),
    SIGN_IN("/signin"),
    TABLES("/tables"),
    TABLE_BY_ID("/tables/{tableId}"),
    RESERVATIONS("/reservations");

    private final String path;

    ResourcePath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}