package com.kamegatze.authorization.transfer.client;

public interface ClientTransfer <T> {
    void sendData(T body, String topic);
}
