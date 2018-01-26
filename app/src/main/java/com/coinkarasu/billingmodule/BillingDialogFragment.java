package com.coinkarasu.billingmodule;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.coinkarasu.utils.CKLog;

public class BillingDialogFragment extends DialogFragment {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "BillingDialogFragment";

    private String message;

    public static BillingDialogFragment newInstance(String message) {
        BillingDialogFragment fragment = new BillingDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            message = getArguments().getString("message");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setPositiveButton("OK", null);
        return builder.create();
    }
}
