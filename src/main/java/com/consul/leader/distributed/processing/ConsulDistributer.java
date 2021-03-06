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

    private Map<String, ServiceDefinition> services = new Hashtable<String, ServiceDefinition>();
    private List<DistributedOperation> operations = new ArrayList<DistributedOperation>();
    private BlockingQueue<ServiceDefinition> serviceQueue = new LinkedBlockingQueue<>();
    private Lock lock = new ReentrantLock();
    private volatile int numberOfCompletedRequests = 0;
    private CountDownLatch latch = new CountDownLatch(1);
    private CountDownLatch serviceBuildinglatch;
    private int IdGenerator = -1;

    public ConsulDistributer() {
        this.lock.lock();
    }

    private void reset() {
        this.serviceQueue.clear();
        this.services.clear();
    }

    private ServiceDefinition getService(String serviceId) {
        return this.getServices().get(serviceId);
    }

    public List<DistributedOperation> getOperations() {
        return this.operations;
    }

    private void addServiceToMap(ServiceDefinition serviceDef) {
        this.getServices().put(serviceDef.getMetadata().get("service.id"), serviceDef);
    }

    public void addNewOperation(ServantRequest request) {
        this.operations.add(new DistributedOperation(request));
    }

    public boolean isProcessingCompleted() {
        return (this.getTotalNumberOfOperaions() == this.numberOfCompletedRequests) ? true : false;
    }

    protected void incrementCompletedOperations() {
        this.numberOfCompletedRequests++;
    }



    public int getServiceQueueSize() {
        waitForLatch(serviceBuildinglatch);
        return this.serviceQueue.size();
    }

    public int getNumberOfCompletedRequests() {
        return numberOfCompletedRequests;
    }

    public int getTotalNumberOfOperaions() {
        return this.operations.size();
    }

    public Map<String, ServiceDefinition> getServices() {
        waitForLatch(serviceBuildinglatch);
        return services;
    }

    protected int generateNewRequestId() {
        IdGenerator++;
        return IdGenerator;
    }

    private void waitForLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    /*
     * TODO -> Check if service is available
     */
    public void setFreeServiceToQueue(String serviceId) {
        waitForLatch(serviceBuildinglatch);
        this.serviceQueue.add(this.getService(serviceId));
    }

    public ServiceDefinition pickFreeService() {
        waitForLatch(serviceBuildinglatch);
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

    public void builServicesList(String tageName, String tagValue)
            throws NoAvailableServantsException {
        serviceBuildinglatch = new CountDownLatch(1);
        reset();
        LeaderObserver.getInstance().getServentListByTag(tageName, tagValue).stream()
                .filter(service -> service != null).forEach(service -> {
                    if (service != null) {
                        this.addServiceToMap(service);
                        this.serviceQueue.add(service);
                    }
                });
        serviceBuildinglatch.countDown();
        if (this.serviceQueue.size() <= 0) {
            throw new NoAvailableServantsException();
        }
    }

    public void builServicesList() throws NoAvailableServantsException {
        serviceBuildinglatch = new CountDownLatch(1);
        reset();
        LeaderObserver.getInstance().getAllServentList().stream().filter(service -> service != null)
                .forEach(service -> {
                    if (service != null) {
                        this.addServiceToMap(service);
                        this.serviceQueue.add(service);
                    }

                });
        serviceBuildinglatch.countDown();
        if (this.serviceQueue.size() <= 0) {
            throw new NoAvailableServantsException();
        }
    }

    public Optional<?> waitAndCallMyProcessDistributedResults(Watcher watcher) {
        System.out.println("start waiting lock");

        while (!this.isProcessingCompleted()) {
            waitForLatch(this.latch);
        }
        System.out.println("enf waiting lock");
        return watcher.processDistributedOperations(this.operations);
    }

    public void waitDistributedResults() {
        System.out.println("start waiting lock");
        while (!this.isProcessingCompleted()) {
            waitForLatch(this.latch);
        }
        System.out.println("end waiting lock");
    }



}
