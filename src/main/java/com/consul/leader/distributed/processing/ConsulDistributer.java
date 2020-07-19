package com.consul.leader.distributed.processing;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import com.consul.leader.elections.exception.MissingRequestIdInResponseException;
import com.consul.leader.elections.exception.NoAvailableServantsException;
import com.consul.leader.elections.leader.LeaderObserver;
import com.consul.leader.elections.leader.Watcher;
import com.consul.leader.elections.services.ServiceDefinition;

public class ConsulDistributer {

    private Map<String, ServiceDefinition> services = new Hashtable<String, ServiceDefinition>();
    private Map<Integer, DistributedOperation> uncompletedOperations =
            new Hashtable<Integer, DistributedOperation>();
    private Map<Integer, DistributedOperation> completedOperations =
            new Hashtable<Integer, DistributedOperation>();
    private BlockingQueue<ServiceDefinition> serviceQueue = new LinkedBlockingQueue<>();
    private Lock lock = new ReentrantLock();
    // private volatile int numberOfCompletedRequests = 0;
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

    public List<DistributedOperation> getCompletedOperations() {
        return this.completedOperations.values().stream().collect(Collectors.toList());
    }

    public List<DistributedOperation> getUnCompletedOperations() {
        return this.uncompletedOperations.values().stream().collect(Collectors.toList());
    }

    private void addServiceToMap(ServiceDefinition serviceDef) {
        // System.out.println("Here build3");
        this.services.put(serviceDef.getMetadata().get("service.id"), serviceDef);
        // System.out.println("Here build4");
    }

    public void addNewOperation(ServantRequest request) {
        this.uncompletedOperations.put(request.getRequestID(), new DistributedOperation(request));
    }

    public boolean isProcessingCompleted() {
        return (this.uncompletedOperations.size() == 0) ? true : false;
    }



    public int getServiceQueueSize() {
        waitForLatch(serviceBuildinglatch);
        return this.serviceQueue.size();
    }

    public int getNumberOfCompletedOperations() {
        return this.completedOperations.size();
    }

    public int getNumberOfUnCompletedOperations() {
        return this.uncompletedOperations.size();
    }


    public int getTotalNumberOfOperaions() {
        return this.completedOperations.size() + this.uncompletedOperations.size();
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


    public void completeOperation(int requestId, ServantResponse servantResponse)
            throws MissingRequestIdInResponseException {
        if (requestId == -1) {
            throw new MissingRequestIdInResponseException();
        }
        /*
         * this.uncompletedOperations.stream().forEach( op ->
         * System.out.println("op.getServantRequest()" + op.getServantRequest()));
         * this.uncompletedOperations.stream().forEach(op -> System.out.println(
         * "op.getServantRequest().getRequestID()" + op.getServantRequest().getRequestID()));
         */
        DistributedOperation operation = this.uncompletedOperations.get(requestId);

        operation.setServantResponse(servantResponse);
        setFreeServiceToQueue(operation.getServantRequest().getTagertServiceID());
        // System.out.println("getNumberOfCompletedRequests" +
        // this.getNumberOfCompletedOperations());
        // synchronized (operation) {
        this.completedOperations.put(requestId, operation);
        this.uncompletedOperations.remove(requestId);
        // }
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
        // System.out.println("Here building");
        serviceBuildinglatch = new CountDownLatch(1);
        reset();
        LeaderObserver.getInstance().getServentListByTag(tageName, tagValue).stream()
                .filter(service -> service != null).forEach(service -> {
                    if (service != null) {
                        // System.out.println("Here building" + service.getHost() +
                        // service.getPort());
                        this.addServiceToMap(service);
                        // System.out.println("Here build1");
                        this.serviceQueue.add(service);
                        // System.out.println("Here build2");
                    }
                });
        // System.out.println("done building");
        serviceBuildinglatch.countDown();
        if (this.serviceQueue.size() <= 0) {
            // System.out.println("ssevice num 0");
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
        waitDistributedResults();
        return watcher.processDistributedOperations(
                this.completedOperations.values().stream().collect(Collectors.toList()));
    }

    public void waitDistributedResults() {
        // System.out.println("start waiting lock");
        while (!this.isProcessingCompleted()) {
            waitForLatch(this.latch);
        }
        // System.out.println("end waiting lock");
    }



}
