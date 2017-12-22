package com.lobbyassist.model;

public class StunAck {

    private long when;
    private long last = System.currentTimeMillis();

    public StunAck (long when) {
        this.when = when;
    }

    public void request (long when) {
        this.when = when;
    }

    public long ack(long when) {
        long ack = when - this.when;
        this.when = 0;
        this.last = System.currentTimeMillis();
        return ack;
    }

    public boolean isWaiting () {
        return when > 0;
    }

    public boolean isExpired () {
        return (System.currentTimeMillis() - last) / 1000 > 5;
    }
}
