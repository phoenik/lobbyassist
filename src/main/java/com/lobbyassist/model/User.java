package com.lobbyassist.model;

import javafx.beans.property.*;

public class User {
    public enum FILTER {
        USER(-1),
        LIKED(0),
        BLOCKED(1);

        private final int code;

        FILTER (int code) {
            this.code = code;
        }

        public static FILTER fromCode (int code) {
            for(FILTER filter : FILTER.values()) {
                if(code == filter.code) {
                    return filter;
                }
            }
            return USER;
        }
    }

    private int id = 0;
    private StringProperty display = new SimpleStringProperty();
    private LongProperty ping = new SimpleLongProperty(0);

    private FILTER filter = FILTER.USER;

    public User (int id) {
        this.id = id;
    }

    private void setFilter (FILTER filter) {
        this.filter = filter;
        this.display.setValue(this.filter.name());
    }

    public StringProperty displayProperty () {
        return display;
    }

    public LongProperty pingProperty () {
        return ping;
    }

    public void reset () {
        this.setFilter(FILTER.USER);
    }

    public void liked () {
        this.setFilter(FILTER.LIKED);
    }

    public void block () {
        this.setFilter(FILTER.BLOCKED);
    }
}
