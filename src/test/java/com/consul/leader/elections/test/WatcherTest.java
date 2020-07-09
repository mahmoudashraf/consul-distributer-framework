package com.consul.leader.elections.test;


import java.util.List;
import org.springframework.stereotype.Component;
import com.consul.leader.distributed.processing.DistributedOperation;
import com.consul.leader.elections.annotation.OnLeader;
import com.consul.leader.elections.annotation.OnServent;
import com.consul.leader.elections.dto.Leader;
import com.consul.leader.elections.exception.LeaderNotPresented;
import com.consul.leader.elections.leader.Watcher;
import com.consul.leader.elections.services.ServiceDefinition;

@Component
public class WatcherTest implements Watcher {


    private boolean isLeader = false;

    public WatcherTest() {
        start();
    }

    @OnLeader
    public List<ServiceDefinition> getServents() {
        return getwatcher().getAllServentList();
    }

    @OnServent
    public Leader doServent() throws LeaderNotPresented {
        return getwatcher().getCurrentLeader();
    }

    @Override
    public void onGrantedLeaderNotification() {
        setLeader(true);
    }

    @Override
    public void newLeaderNotification() {
        setLeader(false);
    }

    @OnLeader
    public void stepDown() {
        getwatcher().stepDownAndWaitnewLeader();
    }

    @OnServent
    public void stepUp() {
        getwatcher().getServiceNode().enableLeadership();
    }

    public boolean isLeader() {
        return isLeader;
    }

    private void setLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }

    @Override
    public Object receiveProcessingResult(List<DistributedOperation> operations) {
        // TODO Auto-generated method stub
        return new Object();

    }


}
