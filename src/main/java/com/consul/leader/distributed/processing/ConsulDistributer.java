package com.consul.leader.distributed.processing;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.consul.leader.elections.exception.MissingRequestIdInResponseException;
import com.consul.leader.elections.exception.NoAvailableServantsException;
import com.consul.leader.elections.leader.LeaderObserver;
import com.consul.leader.elections.leader.Watcher;
import com.consul.leader.elections.services.ServiceDefinition;

public class ConsulDistributer {

    public ConsulDistributer() {
        this.lock.lock();
    }

    private Map<String, ServiceDefinition> services = new Hashtable<String, ServiceDefinition>();
    private List<DistributedOperation> operations = new ArrayList<DistributedOperation>();
    private BlockingQueue<ServiceDefinition> serviceQueue = new LinkedBlockingQueue<>();
    private Lock lock = new ReentrantLock();
    private volatile int numberOfCompletedRequests = 0;
    private CountDownLatch latch = new CountDownLatch(1);

    private int IdGenerator = -1;

    public int getNumberOfCompletedRequests() {
        return numberOfCompletedRequests;
    }


    private void addServiceToMap(ServiceDefinition serviceDef) {
        this.getServices().put(serviceDef.getMetadata().get("service.id"), serviceDef);
    }

    private ServiceDefinition getService(String serviceId) {
        return this.getServices().get(serviceId);
    }


    public void addNewOperation(ServantRequest request) {
        this.operations.add(new DistributedOperation(request));
    }

    synchronized public void completeOperation(int leaderRequestId, ServantResponse servantResponse)
            throws MissingRequestIdInResponseException {
        incrementCompletedOperations();
        if (leaderRequestId == -1) {
            throw new MissingRequestIdInResponseException();
        }
        DistributedOperation operation = this.operations.get(leaderRequestId);
        operation.setServantResponse(servantResponse);
        setFreeServiceToQueue(operation.getServantRequest().getTagertServiceID());
        System.out.println("getNumberOfCompletedRequests" + this.getNumberOfCompletedRequests());
        if (this.isProcessingCompleted()) {
            latch.countDown();
        }
    }


    protected void incrementCompletedOperations() {
        this.numberOfCompletedRequests++;
    }

    public boolean isProcessingCompleted() {
        return (this.getTotalNumberOfOperaions() == this.numberOfCompletedRequests) ? true : false;
    }

    public void setFreeServiceToQueue(String serviceId) {
        this.serviceQueue.add(this.getService(serviceId));
    }

    public ServiceDefinition pickFreeService() {
        ServiceDefinition take = null;
        while (take == null) {
            try {
                take = this.serviceQueue.take();
                return take;
            } catch (InterruptedException e) {

            }
        }
        return take;
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
        numberOfCompletedRequests = 0;

    }

    public void builServicesList(String tageName, String tagValue)
            throws NoAvailableServantsException {

        reset();
        LeaderObserver.getInstance().getServentListByTag(tageName, tagValue).stream()
                .filter(service -> service != null).forEach(service -> {
                    if (service != null)
                        this.addServiceToMap(service);
                    this.serviceQueue.add(service);
                });
        if (this.serviceQueue.size() <= 0) {
            throw new NoAvailableServantsException();
        }
    }

    public void builServicesList() {
        LeaderObserver.getInstance().getAllServentList().stream().filter(service -> service != null)
                .forEach(service -> {
                    if (service != null)
                        this.addServiceToMap(service);
                });
    }

    public Optional<?> waitAndCallMyProcessDistributedResults(Watcher watcher) {
        System.out.println("start waiting lock");

        while (!this.isProcessingCompleted()) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("enf waiting lock");
        return watcher.processDistributedOperations(this.operations);
    }

    public void waitDistributedResults() {
        System.out.println("start waiting lock");
        while (!this.isProcessingCompleted()) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("enf waiting lock");
    }


    public Map<String, ServiceDefinition> getServices() {
        return services;
    }

    public int generateNewRequestId() {
        IdGenerator++;
        return IdGenerator;
    }

}
