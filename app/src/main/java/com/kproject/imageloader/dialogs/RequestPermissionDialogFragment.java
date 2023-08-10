package com.kproject.imageloader.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import com.kproject.imageloader.R;
import com.kproject.imageloader.utils.Utils;

public class RequestPermissionDialogFragment extends DialogFragment {
    private RequestPermissionListener dialogListener;

    public static RequestPermissionDialogFragment newInstance() {
        return new RequestPermissionDialogFragment();
    }

    public interface RequestPermissionListener {
        public void onClickRequestPermissionDialog(DialogInterface dialog);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            dialogListener = (RequestPermissionListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), Utils.setThemeForDialog());
        dialog.setTitle(getResources().getString(R.string.dialog_request_permission));
        dialog.setMessage(getResources().getString(R.string.dialog_request_permission_info));
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                dialogListener.onClickRequestPermissionDialog(dialogInterface);
            }
        });
        dialog.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                dialogInterface.dismiss();
            }
        });

        return dialog.create();
    }

}
