package com.consul.leader.elections.test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.consul.leader.elections.dto.Leader;
import com.consul.leader.elections.exception.LeaderNotPresented;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@EnableAutoConfiguration

public class LeaderObserverTests {

    @Autowired
    private LeaderObserverTestHelper testUtil;

    @Autowired
    WatcherTest watcherTest;

    /*
     * @Before(set service node info
     * {"ipAddress":"127.0.0.1","port":8080,"nodeId":"localhost-8080"}") E2E Scenarios:
     * 
     * @Before(Live Consul Leader values is {"ipAddress":"live1","port":8081,"nodeId":"id1"})
     * 1-"StartUp With Valid Leader exist values observer run -> receive valid values from listner
     * ->isgranted returns false ->new leader configred notification ->watcher isleader assigned
     * false -> observer leader is equal to live Consul Leader key
     */

    @Test
    public void startUpWithValidLeaderValues() throws LeaderNotPresented {
        testUtil.deleteLeaderFromConsul();
        Leader leader = new Leader("live1", 8081, "id1");
        testUtil.setLeaderInConsul(leader);
        testUtil.publishWebServerInitializedEventCustom();
        // wait until listner get consul values
        testUtil.waitForMillisecond(5000);
        assertEquals(testUtil.getLeaderObserver().isGrantedLeader(), false);
        assertEquals(testUtil.getLeaderObserver().getCurrentLeader().isEmpty(), false);
        assertEquals(watcherTest.isLeader(), false);
        assertEquals(testUtil.leaderToJson(testUtil.getLeaderObserver().getCurrentLeader()),
                testUtil.getLeaderFromConsul());

    }

    /*
     * @Before(Live Consul Leader values is {"ipAddress":"Live2","port":808") 2-"StartUp With
     * InValid Leader values observer run -> receive invalid values from listner ->volounteer start
     * -> granted Leader notification ->watcher isleader assigned true -> observer leader isEmpty
     * return false -> watcher isleader assigned true ->observer leader is equal to live Consul
     * Leader key
     */
    @Test
    public void startUpWithInValidLeaderValues() throws LeaderNotPresented {
        testUtil.deleteLeaderFromConsul();
        Leader leader = new Leader("", 0, "id1");
        testUtil.setLeaderInConsul(leader);
        testUtil.publishWebServerInitializedEventCustom();
        // wait until listner get consul values
        testUtil.waitForMillisecond(10000);
        assertEquals(testUtil.getLeaderObserver().isGrantedLeader(), true);
        assertEquals(testUtil.getLeaderObserver().getCurrentLeader().isEmpty(), false);

        assertEquals(watcherTest.isLeader(), true);
        assertEquals(testUtil.leaderToJson(testUtil.getLeaderObserver().getCurrentLeader()),
                testUtil.getLeaderFromConsul());

    }

    /*
     * @Before(Live Consul Leader values is {"ipAddress":"Live2","port":808") 2-"StartUp With
     * InValid Leader values observer run -> receive valid values from listner ->isgranted returns
     * false ->new leader configred notification ->watcher isleader assigned false -> observer
     * leader isEmpty return true -> watcher isleader assigned false ->volounteer start -> granted
     * Leader notification ->watcher isleader assigned true -> observer leader isEmpty return false
     * -> watcher isleader assigned true ->observer leader is equal to live Consul Leader key
     */
    @Test
    public void startUpWithInEmptyLeaderValues() throws LeaderNotPresented {
        testUtil.deleteLeaderFromConsul();
        testUtil.setLeaderInConsul("");
        testUtil.publishWebServerInitializedEventCustom();
        // wait until listner get consul values
        testUtil.waitForMillisecond(10000);
        assertEquals(testUtil.getLeaderObserver().isGrantedLeader(), true);
        assertEquals(testUtil.getLeaderObserver().getCurrentLeader().isEmpty(), false);
        assertEquals(watcherTest.isLeader(), true);
        assertEquals(testUtil.leaderToJson(testUtil.getLeaderObserver().getCurrentLeader()),
                testUtil.getLeaderFromConsul());

    }


}

