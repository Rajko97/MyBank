package com.ana.mybank.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.ana.mybank.R;
import com.ana.mybank.model.CreditCards;
import com.ana.mybank.model.UserPojo;
import com.ana.mybank.ui.login.LoginActivity;
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

import java.util.ArrayList;
import java.util.List;

import static com.ana.mybank.Constants.getMoneyFormat;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SliderAdapter sliderAdapter;

    private UserPojo userData;
    private ArrayList<CreditCards> creditCardsData;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        getUserDataFromFireStore();
    }

    private void getUserDataFromFireStore() {
        final DocumentReference docRef = db.collection("Users").document(mAuth.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        try {
                            userData = task.getResult().toObject(UserPojo.class);
                            initializeView(docRef);
                        } catch (ClassCastException e) {
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



    private void initializeView(DocumentReference userDocumentReference) {
        setContentView(R.layout.activity_main);
        setToolbar(userDocumentReference);
        setFloatingActionButton();
        setSlideViewPager();
    }

    private void setSlideViewPager() {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        if (userData.getCreditCards() == null || userData.getCreditCards().size() == 0) {
            TextView textView = findViewById(R.id.textViewError);
            textView.setVisibility(View.VISIBLE);
            return;
        }
        List<CreditCards> testCards = new ArrayList();
        sliderAdapter = new SliderAdapter(MainActivity.this, null);
        ViewPager slideViewPager = (ViewPager) findViewById(R.id.viewPager);
        slideViewPager.setAdapter(sliderAdapter);
        getCardsData();
    }


    private void setToolbar(DocumentReference userDocumentReference) {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setOnMenuItemClickListener(this);
        final TextView balanceTextView = toolbar.findViewById(R.id.textViewBalance);
        toolbar.setTitle(""+userData.getName()+" "+userData.getLastName());
        toolbar.setSubtitle("ID: "+userData.getAccountNumber());
        balanceTextView.setText(getMoneyFormat(userData.getBalance()));
        userDocumentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
    }

    private void setFloatingActionButton() {
        ExtendedFloatingActionButton extendedFloatingActionButton = findViewById(R.id.fabMakePayment);
        extendedFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PaymentBottomSheetDialogFragment fragment = new PaymentBottomSheetDialogFragment();
                fragment.show(getSupportFragmentManager(), fragment.getTag());
            }
        });
    }

    private void getCardsData() {
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String cardId: userData.getCreditCards()) {
            DocumentReference documentReference = db.collection("CreditCards").document(cardId);
            Task<DocumentSnapshot> documentSnapshotTask = documentReference.get();
            tasks.add(documentSnapshotTask);
        }

        creditCardsData = new ArrayList<CreditCards>();
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> list) {
                for (Object item: list) {
                    DocumentSnapshot cardDocument = (DocumentSnapshot) item;
                    String cardId = cardDocument.getId();
                    String securityCode = (String) cardDocument.get("securityCode");
                    List<DocumentReference> transactionsDocuments = (List<DocumentReference>) cardDocument.get("transactions");
                    creditCardsData.add(new CreditCards(cardId, securityCode, transactionsDocuments));
                }
                sliderAdapter.updateDataSet(creditCardsData);
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
