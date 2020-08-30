package com.ana.mybank.model;

import java.util.ArrayList;

public class UserPojo {
    private String name;
    private String lastName;
    private String accountNumber;
    private Double balance;
    private ArrayList<String> creditCards;

    public UserPojo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public ArrayList<String> getCreditCards() {
        return creditCards;
    }

    public void setCreditCards(ArrayList<String> creditCards) {
        this.creditCards = creditCards;
    }
}