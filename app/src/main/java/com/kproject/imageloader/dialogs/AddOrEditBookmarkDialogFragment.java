package com.kproject.imageloader.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import com.kproject.imageloader.R;
import com.kproject.imageloader.utils.Utils;

public class AddOrEditBookmarkDialogFragment extends DialogFragment {
    private DialogListener dialogListener;

    public AddOrEditBookmarkDialogFragment() {
    }

    public static AddOrEditBookmarkDialogFragment newInstance(String title, String pageUrl, int operationType, int bookmarkId) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("pageUrl", pageUrl);
        bundle.putInt("operationType", operationType);
        bundle.putInt("bookmarkId", bookmarkId);
        AddOrEditBookmarkDialogFragment dialog = new AddOrEditBookmarkDialogFragment();
        dialog.setArguments(bundle);
        return dialog;
    }

    public interface DialogListener {
        void onSaveBookmark(String title, String pageUrl,
                            int operationType, int bookmarkId)
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dialogListener = (AddOrEditBookmarkDialogFragment.DialogListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dialogListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String pageUrl = getArguments().getString("pageUrl");
        final int operationType = getArguments().getInt("operationType");
        final Integer bookmarkId = getArguments().getInt("bookmarkId");
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), Utils.setThemeForDialog());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_or_edit_bookmark, null);
        final TextInputEditText edTitle = view.findViewById(R.id.edAddOrEditBookmark_Title);
        final TextInputEditText edPageUrl = view.findViewById(R.id.edAddOrEditBookmark_PageUrl);
        if (!title.isEmpty() && !pageUrl.isEmpty()) {
            dialog.setTitle(getResources().getString(R.string.dialog_edit_bookmark));
            edTitle.setText(title);
            edPageUrl.setText(pageUrl);
        } else {
            dialog.setTitle(getResources().getString(R.string.dialog_add_bookmark));
        }
        dialog.setView(view);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                dialogListener.onSaveBookmark(edTitle.getText().toString(), edPageUrl.getText().toString(),
                        operationType, bookmarkId);
                dialogInterface.dismiss();
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
