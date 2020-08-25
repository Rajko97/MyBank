package com.ana.mybank.model;


import com.google.firebase.Timestamp;

public class TransactionPojo {
    private String accountId, reason;
    private Timestamp time;
    private Double amount;

    public TransactionPojo() {}

    public TransactionPojo(String accountId, String reason, Timestamp time, Double amount) {
        this.accountId = accountId;
        this.reason = reason;
        this.time = time;
        this.amount = amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }
}
