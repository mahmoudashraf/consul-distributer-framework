package com.consul.leader.distributed.processing;

public class ServantResponse {
    private long requestID;

    public long getRequestID() {
        return requestID;
    }

    public void setRequestID(long requestID) {
        this.requestID = requestID;
    }
}
