package com.ana.mybank.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ana.mybank.R;

import java.util.ArrayList;

class CardsRecyclerAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<String> cardNumber;
    ArrayList<String> cardPin;

    CardsRecyclerAdapter() {
        cardNumber = new ArrayList<>();
        cardPin = new ArrayList<>();
    }

    void addCard(String card, String pin) {
        cardNumber.add(0, card);
        cardPin.add(0, pin);
        notifyItemInserted(0);
    }

    void removeItem(int adapterPosition) {
        cardNumber.remove(adapterPosition);
        cardPin.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
    }


    class CardsViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardNumber;

        CardsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardNumber = itemView.findViewById(R.id.textViewCardNumber);
        }

        void bind(String cardNumber) {
            tvCardNumber.setText("Card: "+cardNumber);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CardsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_create_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof CardsViewHolder) {
            ((CardsViewHolder) holder).bind(cardNumber.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return cardNumber == null? 0 : cardNumber.size();
    }
}
