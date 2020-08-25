package com.ana.mybank.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ana.mybank.R;
import com.ana.mybank.model.TransactionPojo;
import com.ana.mybank.ui.login.LoginActivity;
import com.example.creditcardlibrary.view.CustomCreditCard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.ana.mybank.Constants.getMoneyFormat;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CustomCreditCard creditCard;
    private TransactionsRecyclerAdapter transactionsRecyclerAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        getDataFromFireStore();
    }

    private void getDataFromFireStore() {
        final DocumentReference docRef = db.collection("Users").document(mAuth.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        try {
                            JSONObject object = new JSONObject(document.getData().toString());
                            String name = object.getString("name");
                            String lastName = object.getString("lastName");
                            String accountNumber = object.getString("accountNumber");
                            String cardNumber = object.getString("cardNumber");
                            Double balance = object.getDouble("balance");
                            initializeView(name, lastName, accountNumber, cardNumber, balance, docRef);
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Failed to parse user data", Toast.LENGTH_SHORT).show();
                            logout();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "User data is deleted", Toast.LENGTH_SHORT).show();
                        logout();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "get failed with " + task.getException(), Toast.LENGTH_SHORT).show();
                    logout();
                }
            }
        });
    }

    private void initializeView(String name, String lastName, String accountNumber, String cardNumber, final double balance, DocumentReference docRef) {
        setContentView(R.layout.activity_main);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setOnMenuItemClickListener(this);
        final TextView balanceTextView = toolbar.findViewById(R.id.textViewBalance);

        toolbar.setTitle(""+name+" "+lastName);
        toolbar.setSubtitle("ID: "+accountNumber);
        balanceTextView.setText(getMoneyFormat(balance));
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if(error == null) {
                    if(snapshot != null && snapshot.exists()) {
                        Double newBalance = snapshot.getDouble("balance");
                        balanceTextView.setText(getMoneyFormat(newBalance));
                    }
                }
            }
        });

        ExtendedFloatingActionButton extendedFloatingActionButton = findViewById(R.id.fabMakePayment);
        extendedFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PaymentBottomSheetDialogFragment fragment = new PaymentBottomSheetDialogFragment();
                fragment.show(getSupportFragmentManager(), fragment.getTag());
            }
        });
        RecyclerView transactionsRecycler = findViewById(R.id.transactionsRecycler);
        transactionsRecyclerAdapter = new TransactionsRecyclerAdapter();
        transactionsRecycler.setAdapter(transactionsRecyclerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        transactionsRecycler.setLayoutManager(layoutManager);
        transactionsRecycler.addItemDecoration(new DividerItemDecoration(transactionsRecycler.getContext(), layoutManager.getOrientation()));

        creditCard = findViewById(R.id.creditCard);
        AppCompatTextView cardNumberTextView = creditCard.getRootView().findViewById(R.id.creditcard_card_number_label);
        cardNumberTextView.setText(cardNumber);

        getTransactions(cardNumber);
    }

    private void getTransactions(final String cardNumber) {
        final DocumentReference myCardDocumentRef = db.collection("CreditCards").document(cardNumber);
        myCardDocumentRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot cardDocument = task.getResult();

                            AppCompatTextView cardCVV = creditCard.getRootView().findViewById(R.id.creditcard_cvv_label);
                            cardCVV.setText(cardDocument.getString("securityCode"));

                            List<DocumentReference> transactionsDocuments = (List<DocumentReference>) cardDocument.get("transactions");
                            if(transactionsDocuments == null) {
                                createTransactionListener();
                                return;
                            }

                            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                            for(DocumentReference documentReference : transactionsDocuments) {
                                Task<DocumentSnapshot> documentSnapshotTask = documentReference.get();
                                tasks.add(documentSnapshotTask);
                            }
                            Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                                @Override
                                public void onSuccess(List<Object> objects) {
                                    List<TransactionPojo> transactionsList = new ArrayList<>();
                                    for(Object object : objects) {
                                        TransactionPojo transactionPojo = ((DocumentSnapshot) object).toObject(TransactionPojo.class);
                                        transactionsList.add(transactionPojo);
                                    }
                                    transactionsRecyclerAdapter.submitData(transactionsList);
                                    createTransactionListener();
                                }
                            });
                        }
                    }

                    private void createTransactionListener() {
                        myCardDocumentRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                                if(error == null) {
                                    if(snapshot != null && snapshot.exists()) {
                                        List<DocumentReference>  list = (List<DocumentReference>) snapshot.get("transactions");
                                        if(list != null && list.size() != transactionsRecyclerAdapter.getItemCount()) {
                                            getTransactions(cardNumber);
                                        }
                                    }
                                }
                            }
                        });
                    }
                });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menuOptionLogout) {
            logout();
            return true;
        }
        return false;
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}
