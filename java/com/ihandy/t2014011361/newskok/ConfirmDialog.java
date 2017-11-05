package com.ihandy.t2014011361.newskok;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class ConfirmDialog {
    int dialogResult;
    public static Handler mHandler;
    public Context context;

    public static boolean showComfirmDialog(Activity context, String title, String msg) {
        mHandler = new MyHandler();
        return new ConfirmDialog(context, title, msg).getResult() == 1;
    }

    public ConfirmDialog(Activity context, String title, String msg) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setPositiveButton(R.string.confirm_button, new DialogButtonOnClick(1));
        dialogBuilder.setTitle(title).setMessage(msg).setCancelable(false).create().show();
        try {Looper.loop();}catch (Exception e) {}
    }
    public int getResult() {return dialogResult;}

    private static class  MyHandler extends Handler{
        public void handleMessage(Message mesg) {
            throw new RuntimeException();
        }
    }

    private final class DialogButtonOnClick implements OnClickListener{
        int type;
        public DialogButtonOnClick(int type){
            this.type = type;
        }
        public void onClick(DialogInterface dialog, int which) {
            InitActivity.noInternetShowed = false;
            ConfirmDialog.this.dialogResult = type;
            Message m = mHandler.obtainMessage();
            mHandler.sendMessage(m);
        }
    }

}
