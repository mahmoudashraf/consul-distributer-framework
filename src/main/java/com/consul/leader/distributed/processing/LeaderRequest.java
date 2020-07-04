package com.consul.leader.distributed.processing;

public class LeaderRequest {

    private int requestID;
    private String tagertServiceID;

    private static int IdGenerator = 0;

    public LeaderRequest(String tagertServiceID) {
        super();
        this.tagertServiceID = tagertServiceID;
        this.requestID = IdGenerator;
        IdGenerator++;
    }

    public int getRequestID() {
        return requestID;
    }

    public String getTagertServiceID() {
        return tagertServiceID;
    }

    public void setTagertServiceID(String tagertServiceID) {
        this.tagertServiceID = tagertServiceID;
    }

    public static void resetIDGenerator() {
        IdGenerator = 0;
    }
}
