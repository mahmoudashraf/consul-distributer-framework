package com.consul.leader.distributed.processing;

public class DistributedOperation {

    private LeaderRequest leaderRequest;
    private ServantResponse servantResponse;

    public DistributedOperation(LeaderRequest leaderRequest) {
        super();
        this.leaderRequest = leaderRequest;
    }

    public LeaderRequest getLeaderRequest() {
        return leaderRequest;
    }

    public void setLeaderRequest(LeaderRequest leaderRequest) {
        this.leaderRequest = leaderRequest;
    }

    public ServantResponse getServantResponse() {
        return servantResponse;
    }

    public void setServantResponse(ServantResponse servantResponse) {
        this.servantResponse = servantResponse;
    }

}
