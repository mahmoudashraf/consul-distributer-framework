package com.consul.leader.elections.test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
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
public class AllCases {

    @Autowired
    private LeaderObserverTestHelper testUtil;

    @Autowired
    WatcherTest watcherTest;



    @Test
    public void TestAll() throws LeaderNotPresented {
        this.startUpWithEmptyLeaderValues();
        this.startUpWithValidLeaderValues();
        this.startUpWithInValidLeaderValues();
    }
    /*
     * @Before(Live Consul Leader values is {"ipAddress":"Live2","port":808") 2-"StartUp With
     * InValid Leader values observer run -> receive valid values from listner ->isgranted returns
     * false ->new leader configred notification ->watcher isleader assigned false -> observer
     * leader isEmpty return true -> watcher isleader assigned false ->volounteer start -> granted
     * Leader notification ->watcher isleader assigned true -> observer leader isEmpty return false
     * -> watcher isleader assigned true ->observer leader is equal to live Consul Leader key
     */



    public void startUpWithEmptyLeaderValues() throws LeaderNotPresented {
        testUtil.deleteLeaderFromConsul();
        testUtil.setُEmptyLeaderInConsul(testUtil.getServiceNode().getServiceName(), 60);
        testUtil.publishWebServerInitializedEventCustom();
        // wait until listner get consul values
        testUtil.waitForMillisecond(10000);
        testUtil.waitForMillisecond(40000);
        assertEquals(testUtil.getLeaderObserver().getCurrentLeader().isEmpty(), false);
        assertEquals(watcherTest.isLeader(), true);
        assertEquals(testUtil.leaderToJson(testUtil.getLeaderObserver().getCurrentLeader()),
                testUtil.getLeaderFromConsul());
        testUtil.getLeaderObserver().getCurrentLeader().reset();
        testUtil.deleteLeaderFromConsul();
        testUtil.getLeaderObserver().removeListner();
        testUtil.waitForMillisecond(50000);
    }

    public void startUpWithValidLeaderValues() throws LeaderNotPresented {
        Leader leader = new Leader("live1", 8081, "id1");
        leader.setSessionId(testUtil.createSession());
        testUtil.setLeaderInConsul(leader);
        testUtil.publishWebServerInitializedEventCustom();

        // wait until listner get consul values
        testUtil.waitForMillisecond(10000);
        testUtil.waitForMillisecond(40000);
        assertEquals(testUtil.getLeaderObserver().getCurrentLeader().isEmpty(), false);
        assertEquals(watcherTest.isLeader(), false);
        assertEquals(testUtil.leaderToJson(testUtil.getLeaderObserver().getCurrentLeader()),
                testUtil.getLeaderFromConsul());
        testUtil.getLeaderObserver().getCurrentLeader().reset();
        testUtil.deleteLeaderFromConsul();
        testUtil.getLeaderObserver().removeListner();
        testUtil.waitForMillisecond(50000);
    }



    public void startUpWithInValidLeaderValues() throws LeaderNotPresented {
        System.out.println("start chekinggggg ");
        Leader leader = new Leader("", 0, "id1");
        leader.setSessionId(testUtil.createSession());
        testUtil.setLeaderInConsul(leader);
        testUtil.publishWebServerInitializedEventCustom();
        // wait until listner get consul values
        testUtil.waitForMillisecond(40000);
        assertEquals(testUtil.getLeaderObserver().getCurrentLeader().isEmpty(), false);
        assertEquals(watcherTest.isLeader(), true);
        assertEquals(testUtil.leaderToJson(testUtil.getLeaderObserver().getCurrentLeader()),
                testUtil.getLeaderFromConsul());
        testUtil.getLeaderObserver().getCurrentLeader().reset();
        testUtil.deleteLeaderFromConsul();
        testUtil.getLeaderObserver().removeListner();
        testUtil.waitForMillisecond(50000);
    }

    @AfterAll
    protected void afterAll() throws LeaderNotPresented {
        testUtil.getLeaderObserver().removeListner();
        testUtil.getLeaderObserver().getCurrentLeader().reset();
    }



}

