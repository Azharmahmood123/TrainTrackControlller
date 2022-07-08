package com.train.track.controller.dialogs;


import android.app.AlertDialog;
import android.content.Context;

public class Dialogs {

    public static void showAlertWithOneButton(Context context, String message, String title, String buttonPositiveText,
                                              boolean cancelable, final OnDialogSingleButtonClickListener onDialogSingleButtonClickListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(cancelable)
                .setPositiveButton(buttonPositiveText, (dialog, which) -> {
                    if (onDialogSingleButtonClickListener != null)
                        onDialogSingleButtonClickListener.onDialogPositiveButtonClick();
                    dialog.dismiss();
                }).show();
    }

    public static void showAlertWithTwoButton(Context context, String message, String title, String buttonPositiveText, String buttonNegativeText,
                                              final OnDialogButtonClickListener dialogListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(buttonPositiveText, (dialog, which) -> {
                    if (dialogListener != null)
                        dialogListener.onDialogPositiveButtonClick();
                    dialog.dismiss();
                })
                .setNegativeButton(buttonNegativeText, (dialog, which) -> {
                    if (dialogListener != null)
                        dialogListener.onDialogNegativeButtonClick();
                    dialog.dismiss();
                }).show();
    }

    public interface OnDialogSingleButtonClickListener {
        void onDialogPositiveButtonClick();
    }


    public interface OnDialogButtonClickListener {
        void onDialogPositiveButtonClick();

        void onDialogNegativeButtonClick();
    }

}
