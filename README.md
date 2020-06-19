# Consul-Leader-Election
[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://travis-ci.org/joemccann/dillinger)

# What is Cosnul Server ?
 [Consul](https://www.hashicorp.com/products/consul "Consul") is a distributed system solution providing a full featured control plane with:
- Service discovery 
- Configuration, and segmentation functionality.

Each of these features can be used individually as needed, or they can be used together to build a distributed system.
vis

# Why Leader Election !!!???
  - Distributed System consists of serveral nodes that co-operate to achieve distributed processing tasks
  - Leader devide processing tasks between nodes and gather processing results to expose the final result 

Example:
  If you have 250K documents to search for a specific word and you want to show How many times that word was mentioned inside.
Leader will devide documents between nodes to process and return result to leader

![alt text](https://github.com/mahmoudashraf/consul-leader-election/blob/master/ExampleDiagram.jpg)

# How it works ? 
Our utility make it easy to implement distrbuted apllication aware of current leader and able to voulnteer to be a leader , once leader is selected each node will receive either NewLeaderConfigured or GrantedLeader notification if node is currently selected as leader and should act as leader  

# Implementation 
- Implement Watcher Interface 
- @Override
    @EventListener(IfGrantedLeaderEvent.class)
     public void onGrantedLeaderNotification

- @Override
    @EventListener(NewLeaderConfiguredEvent.class)
    public void newLeaderNotification

- @OnLeader ,OnServent announations to control access to specific methods 
- To get cuurrent leader info "getwatcher().getCurrentLeader()""
- To get a list of servents node "getwatcher().getServentList()""


# Example:
```
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


    public boolean isLeader() {
        return isLeader;
    }

    private void setLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }


}

```
