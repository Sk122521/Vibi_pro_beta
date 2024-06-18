package com.example.Caseapp.model;

public class Acceptor {
    private String acceptorPhone;
    private String acceptorId;

    private String acceptorName;
    private String status;

    // Default constructor required for calls to DataSnapshot.getValue(Acceptor.class)
    public Acceptor() {
    }

    public Acceptor(String acceptorPhone, String acceptorId, String status, String acceptorName) {
        this.acceptorPhone = acceptorPhone;
        this.acceptorId = acceptorId;
        this.status = status;
        this.acceptorName = acceptorName;
    }

    public String getAcceptorPhone() {
        return acceptorPhone;
    }

    public void setAcceptorPhone(String acceptorPhone) {
        this.acceptorPhone = acceptorPhone;
    }

    public String getAcceptorId() {
        return acceptorId;
    }

    public void setAcceptorId(String acceptorId) {
        this.acceptorId = acceptorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAcceptorName() {
        return acceptorName;
    }

    public void setAcceptorName(String acceptorName) {
        this.acceptorName = acceptorName;
    }
}

