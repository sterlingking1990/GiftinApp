package com.giftinapp.business.business;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.giftinapp.business.model.MerchantWalletPojo;
import com.giftinapp.business.R;
import com.giftinapp.business.utility.SessionManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Calendar;
import java.util.Objects;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;

public class WalletInfo extends Fragment {
    public EditText etCardNumber;
    public EditText etCardCvv;
    public Spinner spMonth;
    public Spinner spYear;
    public Button btnProceed;
    public Button btnRefreshWallet;
    public Long walletAmountFromDb;

    public TextView tvWalletAmount;

    public SessionManager sessionManager;

    private Spinner spFundWalletRange;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallet_info, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCardNumber=view.findViewById(R.id.et_main_card);
        etCardCvv=view.findViewById(R.id.et_main_cvv);
        btnProceed=view.findViewById(R.id.btn_process_payment);
        spYear=view.findViewById(R.id.sp_main_year);
        spMonth=view.findViewById(R.id.sp_main_month);
        btnRefreshWallet=view.findViewById(R.id.btn_refresh_wallet);
        tvWalletAmount=view.findViewById(R.id.tv_wallet_amount);

        spFundWalletRange=view.findViewById(R.id.sp_wallet_range);

        sessionManager = new SessionManager(requireContext());


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, getYears());
//here we set the adapter to the year spinner
        spYear.setAdapter(adapter);

        btnProceed.setOnClickListener(v -> chargeCard(Integer.parseInt(spFundWalletRange.getSelectedItem().toString()) * 100));

        btnRefreshWallet.setOnClickListener(v->refreshWallet());

        refreshWallet();
    }

    private void updateWallet(int amountInKobo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        MerchantWalletPojo merchantWalletPojo = new MerchantWalletPojo();
        merchantWalletPojo.merchant_wallet_amount= (long) (walletAmountFromDb + amountInKobo);


        db.collection("merchants").document(Objects.requireNonNull(sessionManager.getEmail())).collection("reward_wallet").document("deposit").set(merchantWalletPojo);

    }

    private void refreshWallet() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // [END get_firestore_instance]

        // [START set_firestore_settings]
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("merchants").document(Objects.requireNonNull(sessionManager.getEmail())).collection("reward_wallet").document("deposit").get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        DocumentSnapshot result = task.getResult();
                        if(result.exists()) {
                            Long amount = (Long) result.get("merchant_wallet_amount");
                            walletAmountFromDb = amount;
                            tvWalletAmount.setText(amount.toString());
                        }
                        else{
                            walletAmountFromDb=0L;
                            tvWalletAmount.setText("0");
                        }
                    }
                    else {
                        walletAmountFromDb=0L;
                        tvWalletAmount.setText("0");
                    }
                });

    }

    private String[] getYears() {
        String[] years = new String[10];
        Integer year = Calendar.getInstance().get(Calendar.YEAR);
        years[0] = "Year";
        for (int x = 1; x < years.length; x++) {
            String currentYear = String.valueOf(year++);
            years[x] = currentYear;
        }
        return years;
    }

    private void chargeCard(int amountInKobo) {

        String cardCvv = etCardCvv.getText().toString().trim();
        String cardNumber = etCardNumber.getText().toString().trim();
        String month, year;


        if (spMonth.getSelectedItemPosition() > 0 && spYear.getSelectedItemPosition() > 0) {
            month = spMonth.getSelectedItem().toString();
            year = spYear.getSelectedItem().toString();

                if (!cardNumber.isEmpty() && !cardCvv.isEmpty()) {

                    //here we pass the details to the card object
                    Card card = new Card(cardNumber, Integer.valueOf(month), Integer.valueOf(year), cardCvv);

                    //check if the card is valid before attempting to charge the card
                    if (card.isValid()) {
                        //we disable the button so the user doesn't tap multiple times and create a duplicate transaction
                        btnProceed.setEnabled(false);

                        //every transaction requires you to send along a unique reference
                        String customRef = generateReference();

                        //setup a charge object to set values like amount, reference etc
                        Charge charge = new Charge();
                        //the amount(in KOBO eg 1000 kobo = 10 Naira) the customer is to pay for the product or service
                        // basically add 2 extra zeros at the end of your amount to convert from kobo to naira.
                        charge.setAmount(amountInKobo);
                        charge.setReference(customRef);
                        charge.setCurrency("NGN");
                        charge.setCard(card);
                        charge.setEmail(sessionManager.getEmail());

                        //Charge the card
                        PaystackSdk.chargeCard(requireActivity(), charge, new Paystack.TransactionCallback() {
                            @Override
                            public void onSuccess(Transaction transaction) {
                                btnProceed.setEnabled(true);
                                snackBar("Successfully funded your wallet, you can add brand stories now");
                                updateWallet(amountInKobo/100);
                                refreshWallet();
                            }

                            @Override
                            public void beforeValidate(Transaction transaction) {
                                snackBar("beforeValidate");
                            }

                            @Override
                            public void onError(Throwable error, Transaction transaction) {
                                btnProceed.setEnabled(true);
                                snackBar(error.getMessage());

                            }
                        });
                    } else {
                        snackBar("Invalid Card");
                    }
                }
                else {
                    snackBar("Should enter Cvv and Card Number");
                }
            }
        else {
            snackBar("Select Card Expiry Date");
        }
    }


    private String generateReference() {
        String keys = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            int index = (int)(keys.length() * Math.random());
            sb.append(keys.charAt(index));
        }

        return sb.toString();
    }


    private void snackBar(String msg) {
        Snackbar.make(etCardCvv, msg, Snackbar.LENGTH_LONG).show();
    }

}