package com.ana.mybank.ui.main;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ana.mybank.R;
import com.ana.mybank.model.TransactionPojo;
import com.google.firebase.Timestamp;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ana.mybank.Constants.getMoneyFormat;

public class TransactionsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<TransactionPojo> transactionsList = new ArrayList<>();

    void submitData(List<TransactionPojo> transactionsList) {
        this.transactionsList = transactionsList;
        notifyDataSetChanged();
    }

    class TransactionsViewHolder extends RecyclerView.ViewHolder {
        TextView tvAccountId, tvAmount, tvReason, tvTime;

        TransactionsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountId = itemView.findViewById(R.id.textViewAccountId);
            tvAmount = itemView.findViewById(R.id.textViewAmount);
            tvReason = itemView.findViewById(R.id.textViewReason);
            tvTime = itemView.findViewById(R.id.textViewDate);
        }

        void bind(TransactionPojo transactionData) {
            tvAccountId.setText("ID: "+transactionData.getAccountId());
            tvAmount.setText("-"+getMoneyFormat(transactionData.getAmount()));
            tvReason.setText(transactionData.getReason());
            tvTime.setText(getFormattedDate(transactionData.getTime()));
        }

        private String getFormattedDate(Timestamp time) {
            Date date = time.toDate();
            return new SimpleDateFormat("dd-MM-yy\nhh:mm").format(date);
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransactionsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof TransactionsViewHolder) {
            ((TransactionsViewHolder) holder).bind(transactionsList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return transactionsList.size();
    }
}
