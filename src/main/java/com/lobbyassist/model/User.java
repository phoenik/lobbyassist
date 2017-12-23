package com.lobbyassist.model;
import java.io.Serializable;

public class User implements Serializable{
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

    private int id;
    private Long ping;
    private FILTER filter = FILTER.USER;

    public User (int id, long ping) {
        this.id = id;
        this.ping = ping;
    }

    public void setPing (Long ping) {
        this.ping = ping;
    }

    public Long getPing() {
        return ping;
    }

    public FILTER getFilter() {
        return filter;
    }

    public void cycle () {
        this.filter = FILTER.cycle(this.filter);
    }
}
