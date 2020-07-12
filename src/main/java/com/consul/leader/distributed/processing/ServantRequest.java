package com.consul.leader.distributed.processing;

public class ServantRequest {

    private int requestID;
    private String tagertServiceID;


    public ServantRequest() {}

    public ServantRequest(String tagertServiceID, DistributedProcessor processor) {
        super();
        this.tagertServiceID = tagertServiceID;
        this.requestID = processor.generateNewRequestId();
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

}
