package com.ana.mybank.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ana.mybank.R;
import com.ana.mybank.model.TransactionPojo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.type.Date;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    private TextInputLayout layoutName, layoutAmount, layoutPurpose, layoutAccountNumber, layoutCardNumber, layoutPIN;
    private TextInputEditText editName, editAmount, editPurpose, editAccountNumber, editCardNumber, editPin;
    private MaterialButton btnPay;
    private FirebaseFirestore db;
    private DocumentReference documentUser, documentTarget, documentCard;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_dialog_payment, container);
        layoutName = view.findViewById(R.id.inputLayoutName);
        layoutAmount = view.findViewById(R.id.inputLayoutAmount);
        layoutPurpose = view.findViewById(R.id.inputLayoutPurpose);
        layoutAccountNumber = view.findViewById(R.id.inputLayoutAccountNumber);
        layoutCardNumber = view.findViewById(R.id.inputLayoutCardNumber);
        layoutPIN = view.findViewById(R.id.inputLayoutCardPin);

        editName = view.findViewById(R.id.editTextName);
        editAmount = view.findViewById(R.id.editTextAmount);
        editPurpose = view.findViewById(R.id.editTextPurpose);
        editAccountNumber = view.findViewById(R.id.editTextAccountNumber);
        editCardNumber = view.findViewById(R.id.editTextCardNumber);
        editPin = view.findViewById(R.id.editTextCardPin);

        btnPay = view.findViewById(R.id.buttonPay);
        btnPay.setOnClickListener(this);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.buttonPay) {
            String name = editName.getText().toString();
            String amount = editAmount.getText().toString();
            String purpose = editPurpose.getText().toString();
            String accountNumber = editAccountNumber.getText().toString();
            String cardNumber = editCardNumber.getText().toString();
            String pin = editPin.getText().toString();

            boolean isNameValid = validateName(name);
            boolean isAmountValid = validateAmount(amount);
            boolean isValidPurpose = validatePurpose(purpose);
            boolean isValidAccountNumber = validateAccountNumber(accountNumber);
            boolean isValidCardNumber = validateCardNumber(cardNumber);
            boolean isValidPin = validatePin(pin);

            if(isNameValid && isAmountValid && isValidPurpose && isValidAccountNumber && isValidCardNumber && isValidPin) {
                btnPay.setEnabled(false);
                btnPay.setText("WAIT FOR NETWORK");

                Map<String, Object> requestData = new HashMap<>();
                requestData.put("name", name);
                requestData.put("amount", amount);
                requestData.put("purpose", purpose);
                requestData.put("accountNumber", accountNumber);
                requestData.put("cardNumber", cardNumber);
                requestData.put("securityCode", pin);

                findUserByCardNumber(requestData);
            }
        }
    }

    private void findUserByCardNumber(final Map<String, Object> requestData) {
        final String cardNumber = (String) requestData.get("cardNumber");
        db.collection("Users")
                .whereEqualTo("cardNumber", cardNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int error = 1;
                        if(task.isSuccessful()) {
                            if (task.getResult().getDocuments().size() == 1) {
                                try {
                                    DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                    documentUser = document.getReference();

                                    JSONObject cardOwner = new JSONObject(document.getData().toString());

                                    Double ownerBalance = cardOwner.getDouble("balance");
                                    Double amount = Double.valueOf(requestData.get("amount").toString());
                                    error -= 1;

                                    if (ownerBalance < amount) {
                                        error+=2;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        findCardDataByCardNumber(requestData, error);
                        }
                    }
                });

    }

    private void findCardDataByCardNumber(final Map<String, Object> requestData, final int error) {
        if(error != 1 && error != 3) {
            String cardNumber = requestData.get("cardNumber").toString();
            db.collection("CreditCards").document(cardNumber).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {
                                String dbPin = (String) task.getResult().getData().get("securityCode");
                                String reqPin = (String) requestData.get("securityCode");
                                if(!reqPin.equals(dbPin)) {
                                    int newError = error+4;
                                    findTargetByAccountId(requestData, newError);
                                    return;
                                }
                                findTargetByAccountId(requestData, error);
                            }
                        }
                    });
        } else {
            findTargetByAccountId(requestData, error);
        }
    }

    private void findTargetByAccountId(final Map<String, Object> requestData, final int error) {
        String targetAccountId = (String) requestData.get("accountNumber");
        db.collection("Users").whereEqualTo("accountNumber", targetAccountId).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            if (task.getResult().getDocuments().size() == 1) {
                                documentTarget= task.getResult().getDocuments().get(0).getReference();
                               if(error == 0) {
                                  doTransaction(requestData);
                                   return;
                               }
                               handleErrors(error);
                               return;
                            }
                        }
                        handleErrors(error+8);
                    }
                });
    }

    //1 - CardDoesNotExist
    //2 - UserDoesNotHaveMoney
    //4 - Pin
    //8 - TargetDoesNotExist
    private void handleErrors(int errorCode) {
        btnPay.setEnabled(true);
        btnPay.setText("PAY");
        if (errorCode >= 8) {
            errorCode -= 8;
            layoutAccountNumber.setError("Account doesn't exist");
        }
        if(errorCode >= 4) {
            errorCode -= 4;
            layoutPIN.setError("WrongPin");
        }
        if(errorCode >= 2) {
            errorCode -= 2;
            layoutAmount.setError("Not enough funds on card account");
            layoutName.setErrorEnabled(true);
        }
        if(errorCode == 1) {
            layoutCardNumber.setError("Card doesn't exist");
        }
    }

    private void doTransaction(final Map<String, Object> requestData) {
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                double amount = Double.valueOf(requestData.get("amount").toString());
                transaction.update(documentUser, "balance", FieldValue.increment(-amount));
                transaction.update(documentTarget, "balance", FieldValue.increment(amount));

                TransactionPojo newTransaction = new TransactionPojo(
                        requestData.get("accountNumber").toString(),
                        requestData.get("purpose").toString(),
                        Timestamp.now(), /*ToDo replace with: FieldValue.serverTimestamp()*/
                        amount
                );
                DocumentReference newTransactionDocument = db.collection("Transactions").document();
                DocumentReference documentCard = db.collection("CreditCards").document(requestData.get("cardNumber").toString());
                transaction.set(newTransactionDocument, newTransaction);
                transaction.update(documentCard, "transactions", FieldValue.arrayUnion(newTransactionDocument));
                return null;
            }
        });
        this.dismiss();
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialog_Rounded;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), getTheme());
    }

    private boolean validateName(String name) {
        if(TextUtils.isEmpty(name)) {
            layoutName.setError("Enter name");
            return false;
        }
        layoutName.setError(null);
        return true;
    }

    private boolean validateAmount(String amount) {
        if(TextUtils.isEmpty(amount)) {
            layoutAmount.setError("Enter amount");
            return false;
        }
        if(Double.parseDouble(amount) <= 0) {
            layoutAmount.setError("Must greater than 0");
            return false;
        }
        layoutAmount.setError(null);
        return true;
    }

    private boolean validatePurpose(String purpose) {
        if(TextUtils.isEmpty(purpose)) {
            layoutPurpose.setError("Enter comment");
            return false;
        }
        layoutPurpose.setError(null);
        return true;
    }

    private boolean validateAccountNumber(String accountNumber) {
        if(TextUtils.isEmpty(accountNumber)) {
            layoutAccountNumber.setError("Enter account number");
            return false;
        }
        layoutAccountNumber.setError(null);
        return true;
    }

    private boolean validateCardNumber(String cardNumber) {
        if(TextUtils.isEmpty(cardNumber)) {
            layoutCardNumber.setError("Enter card number");
            return false;
        }
        layoutCardNumber.setError(null);
        return true;
    }

    private boolean validatePin(String pin) {
        if(TextUtils.isEmpty(pin)) {
            layoutPIN.setError("Enter PIN");
            return false;
        }
        if(TextUtils.getTrimmedLength(pin) < 3) {
            layoutPIN.setError("Min 3 chars");
            return false;
        }
        layoutPIN.setError(null);
        return true;
    }
}
