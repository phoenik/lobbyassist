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

        public static FILTER cycle (FILTER filter) {
            switch (filter) {
                case USER:
                    return LIKED;
                case LIKED:
                    return BLOCKED;
                case BLOCKED:
                    return USER;
                default:
                    return USER;
            }
        }
    }

    private int id = 0;
    private StringProperty display = new SimpleStringProperty(FILTER.USER.name());
    private LongProperty ping = new SimpleLongProperty(0);

    private FILTER filter = FILTER.USER;

    public User (int id) {
        this.id = id;
    }

    public User (int id, long ping) {
        this.id = id;
        this.pingProperty().setValue(ping);
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

    public void cycle () {
        this.setFilter(FILTER.cycle(this.filter));
    }
}
