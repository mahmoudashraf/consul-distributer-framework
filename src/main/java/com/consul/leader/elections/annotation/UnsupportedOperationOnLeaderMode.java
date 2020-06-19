package com.consul.leader.elections.annotation;

public class UnsupportedOperationOnLeaderMode extends Exception {

    @Override
    public String getMessage() {
        return "UnSupported Operation On Leader Mode";
    }
}
