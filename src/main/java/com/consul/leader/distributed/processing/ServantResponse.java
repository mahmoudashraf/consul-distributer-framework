package com.consul.leader.distributed.processing;

public class ServantResponse {
    private long requestID = -1;

    public ServantResponse() {

    }

    public ServantResponse(long requestID) {
        this.requestID = requestID;
    }

    public long getRequestID() {
        return requestID;
    }

    public void setRequestID(long requestID) {
        this.requestID = requestID;
    }
}
