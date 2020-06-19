package com.consul.leader.elections.exception;


public class MissingLeaderObserverException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String getMessage() {
        return "Observer Not Inialized";
    }
}
