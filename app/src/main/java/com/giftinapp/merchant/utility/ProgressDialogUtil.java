package com.giftinapp.merchant.utility;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogUtil {
    private ProgressDialog dialog;
    private Context context;

    public ProgressDialogUtil(Context context) {
        this.context = context;
        this.dialog = new ProgressDialog(context);
    }

    public void startDialog(String message) {
        if(dialog.isShowing()){
            return;
        }
        dialog.setMessage(message);
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void stopDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
