package com.ana.mybank.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.model.Document;

import java.util.List;

public class CreditCards {
    private String id;
    private String securityCode;
    private List<DocumentReference> transactions;

    public CreditCards() {
    }

    public CreditCards(String cardId, String securityCode, List<DocumentReference> transactionsDocuments) {
        this.id = cardId;
        this.securityCode = securityCode;
        this.transactions = transactionsDocuments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public List<DocumentReference> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<DocumentReference> list) {
        this.transactions = list;
    }
}
