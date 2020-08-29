package com.ana.mybank.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.model.Document;

import java.util.List;

public class CreditCards {
    private String securityCode;
    private List<DocumentReference> list;
}
