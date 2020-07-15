package com.consul.leader.distributed.processing;

public class DistributedOperation {

    private ServantRequest leaderRequest;
    private ServantResponse servantResponse;

    public DistributedOperation(ServantRequest leaderRequest) {
        super();
        this.leaderRequest = leaderRequest;
    }

    public ServantRequest getServantRequest() {
        return leaderRequest;
    }

    public void setLeaderRequest(ServantRequest leaderRequest) {
        this.leaderRequest = leaderRequest;
    }

    public ServantResponse getServantResponse() {
        return servantResponse;
    }

    public void setServantResponse(ServantResponse servantResponse) {
        this.servantResponse = servantResponse;
    }

}
