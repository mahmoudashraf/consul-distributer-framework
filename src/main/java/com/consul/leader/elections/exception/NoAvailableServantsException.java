package com.consul.leader.elections.exception;

public class NoAvailableServantsException extends Exception {

    @Override
    public String getMessage() {
        // TODO Auto-generated method stub
        return "No Servants Available On start";
    }
}
