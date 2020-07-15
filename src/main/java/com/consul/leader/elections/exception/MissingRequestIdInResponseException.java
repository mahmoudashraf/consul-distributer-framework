package com.consul.leader.elections.exception;

public class MissingRequestIdInResponseException extends Exception {

    @Override
    public String getMessage() {
        // TODO Auto-generated method stub
        return "Response is Request Id value,Can't be empty or null ";
    }


}
