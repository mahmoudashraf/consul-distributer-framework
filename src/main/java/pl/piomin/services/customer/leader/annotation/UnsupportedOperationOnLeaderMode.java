package pl.piomin.services.customer.leader.annotation;

public class UnsupportedOperationOnLeaderMode extends Exception {

    @Override
    public String getMessage() {
        return "UnSupported Operation On Leader Mode";
    }
}
