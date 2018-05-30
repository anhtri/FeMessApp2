package com.anhtri.model;

public class Conversations {
    private boolean seen;
    private long timestamp;

    public Conversations(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public Conversations() {
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
