package com.consul.leader.elections.exception;

public class LeaderNotPresented extends Exception {

    @Override
    public String getMessage() {
        // TODO Auto-generated method stub
        return "Leader is not presentd , Check Configuration Or wait For Leader to be presneted";
    }
}
