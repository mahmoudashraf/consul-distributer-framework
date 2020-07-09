package com.consul.leader.distributed.processing;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.consul.leader.elections.leader.LeaderObserver;
import com.consul.leader.elections.leader.Watcher;
import com.consul.leader.elections.services.ServiceDefinition;

public class DistributedProcessor {

    public DistributedProcessor() {
        this.lock.lock();
    }

    private Map<String, ServiceDefinition> services = new Hashtable<String, ServiceDefinition>();
    private List<DistributedOperation> operations = new ArrayList<DistributedOperation>();
    private BlockingQueue<ServiceDefinition> serviceQueue = new LinkedBlockingQueue<>();
    private Lock lock = new ReentrantLock();
    private int numberOfCompletedRequests = 0;
    CountDownLatch latch = new CountDownLatch(1);

    public int getNumberOfCompletedRequests() {
        return numberOfCompletedRequests;
    }


    private void addServiceToMap(ServiceDefinition serviceDef) {
        this.services.put(serviceDef.getMetadata().get("service.id"), serviceDef);
    }

    private ServiceDefinition getService(String serviceId) {
        return this.services.get(serviceId);
    }


    public void addNewOperation(ServantRequest request) {
        this.operations.add(new DistributedOperation(request));
    }

    public void completeOperation(int leaderRequestId, ServantResponse servantResponse) {
        System.out.println("Class Id" + this);
        DistributedOperation operation = this.operations.get(leaderRequestId);
        operation.setServantResponse(servantResponse);
        setFreeServiceToQueue(operation.getServantRequest().getTagertServiceID());
        this.numberOfCompletedRequests++;
        System.out.println("getNumberOfCompletedRequests" + this.getNumberOfCompletedRequests());
        if (this.isProcessingCompleted()) {
            latch.countDown();
        }
    }

    public boolean isProcessingCompleted() {
        return (this.getTotalNumberOfOperaions() == this.numberOfCompletedRequests) ? true : false;
    }

    public void setFreeServiceToQueue(String serviceId) {
        this.serviceQueue.add(this.getService(serviceId));
    }

    public ServiceDefinition pickFreeService() {
        return this.serviceQueue.peek();
    }

    public int getServiceQueueSize() {
        return this.serviceQueue.size();
    }

    public int getTotalNumberOfOperaions() {
        return this.operations.size();
    }

    public List<DistributedOperation> getOperations() {
        return this.operations;
    }

    public void reset() {
        this.operations.clear();
        ServantRequest.resetIDGenerator();
        numberOfCompletedRequests = 0;

    }

    public void builServicesList(String tageName, String tagValue) {
        reset();
        LeaderObserver.getInstance().getServentListByTag(tageName, tagValue).stream()
                .filter(service -> service != null).forEach(service -> {
                    if (service != null)
                        this.addServiceToMap(service);
                    this.serviceQueue.add(service);
                });
    }

    public void builServicesList() {
        LeaderObserver.getInstance().getAllServentList().stream().filter(service -> service != null)
                .forEach(service -> {
                    if (service != null)
                        this.addServiceToMap(service);
                });
    }

    public Object waitAndReceiveProcessingResult(Watcher watcher) {
        System.out.println("start waiting lock");

        while (!this.isProcessingCompleted()) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("enf waiting lock");
        return watcher.receiveProcessingResult(this.operations);
    }
}
