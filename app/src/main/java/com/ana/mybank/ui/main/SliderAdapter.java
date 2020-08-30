package com.ana.mybank.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.ana.mybank.R;
import com.ana.mybank.model.CreditCards;
import com.ana.mybank.model.TransactionPojo;
import com.example.creditcardlibrary.view.CustomCreditCard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class SliderAdapter extends PagerAdapter {
    private int pages = 0;

    Context context;
    LayoutInflater layoutInflater;

    private ArrayList<CreditCards> creditCardsData;
    private ArrayList<ArrayList<TransactionPojo>> transactionsForCardData;
    private ArrayList<TransactionsRecyclerAdapter> transactionsRecyclerAdapters;

    public SliderAdapter(Context context, List<CreditCards> cards) {
        this.context = context;

        if(cards == null) {
            pages = 0;
            creditCardsData = new ArrayList<>(pages);
            transactionsRecyclerAdapters = new ArrayList<TransactionsRecyclerAdapter>(pages);
        }
        else  {
            creditCardsData = new ArrayList(cards);
            pages = creditCardsData.size();
            transactionsRecyclerAdapters = new ArrayList<>(pages);
        }
    }

    public void updateDataSet(ArrayList<CreditCards> creditCardsData) {
        this.creditCardsData.clear();
        this.creditCardsData.addAll(creditCardsData);
        pages = this.creditCardsData.size();
        transactionsRecyclerAdapters = new ArrayList<TransactionsRecyclerAdapter>();
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return pages;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.view_pager_cards, container, false);

        //Credit card data
        CustomCreditCard customCreditCard = view.findViewById(R.id.creditCard);
        AppCompatTextView cardCVV = customCreditCard.getRootView().findViewById(R.id.creditcard_cvv_label);
        cardCVV.setText(creditCardsData.get(position).getSecurityCode());
        AppCompatTextView cardNumberTextView = customCreditCard.getRootView().findViewById(R.id.creditcard_card_number_label);
        cardNumberTextView.setText(creditCardsData.get(position).getId());

        //Transactions
        RecyclerView recyclerView = view.findViewById(R.id.transactionsRecycler);
        TransactionsRecyclerAdapter transactionsRecyclerAdapter = new TransactionsRecyclerAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation()));

        //submit
        transactionsRecyclerAdapters.add(position, transactionsRecyclerAdapter);
        recyclerView.setAdapter(transactionsRecyclerAdapters.get(position));
        setTransactions(transactionsRecyclerAdapters.get(position), creditCardsData.get(position).getId());

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }
    private void setTransactions(final TransactionsRecyclerAdapter adapter, final String cardNumber) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference myCardDocumentRef = db.collection("CreditCards").document(cardNumber);
        myCardDocumentRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot cardDocument = task.getResult();

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
                                    adapter.submitData(transactionsList);
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
                                        if(list != null && list.size() != adapter.getItemCount()) {
                                        setTransactions(adapter, cardNumber);
                                         }
                                    }
                                }
                            }
                        });
                    }
                });
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
