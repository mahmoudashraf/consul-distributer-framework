package pl.piomin.services.customer.test;


import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.piomin.services.customer.dto.Leader;
import pl.piomin.services.customer.event.IfGrantedLeaderEvent;
import pl.piomin.services.customer.event.NewLeaderConfiguredEvent;
import pl.piomin.services.customer.exception.LeaderNotPresented;
import pl.piomin.services.customer.leader.Watcher;
import pl.piomin.services.customer.leader.annotation.OnLeader;
import pl.piomin.services.customer.leader.annotation.OnServent;
import pl.piomin.services.customer.services.ServiceDefinition;

@Component
public class WatcherTest implements Watcher {


    private boolean isLeader = false;

    public WatcherTest() {
        start();
    }

    @OnLeader
    public List<ServiceDefinition> getServents() {
        return getwatcher().getServentList();
    }

    @OnServent
    public Leader doServent() throws LeaderNotPresented {
        return getwatcher().getCurrentLeader();
    }

    @Override
    @EventListener(IfGrantedLeaderEvent.class)
    public void onGrantedLeaderNotification(IfGrantedLeaderEvent event) {
        setLeader(true);
    }

    @Override
    @EventListener(NewLeaderConfiguredEvent.class)
    public void newLeaderNotification(NewLeaderConfiguredEvent event) {
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


}
