package com.kproject.imageloader.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.kproject.imageloader.R;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.Utils;;

public class LoadPageDialogFragment extends DialogFragment {
	private DialogListener dialogListener;
	
	public LoadPageDialogFragment() {}
	
	public static LoadPageDialogFragment newInstance(String pageUrlOrQuery) {
		Bundle bundle = new Bundle();
		bundle.putString("pageUrlOrQuery", pageUrlOrQuery);
		LoadPageDialogFragment dialog = new LoadPageDialogFragment();
		dialog.setArguments(bundle);
		return dialog;
	}
	
	public interface DialogListener {
		void onLoadPageButtonClick(DialogInterface dialogInterface, String pageUrlOrQuery)
	}
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		dialogListener = (LoadPageDialogFragment.DialogListener) context;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		dialogListener = null;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String pageUrlExtra = getArguments().getString("pageUrlOrQuery");
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), Utils.setThemeForDialog());
		dialog.setTitle(R.string.dialog_enter_data);
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_load_page, null);
		final EditText edPageUrlOrQuery = view.findViewById(R.id.edLoadPage_PageUrlOrQuery);
		TextView tvInfo = view.findViewById(R.id.tvLoadPage_Info);
		
		if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
			tvInfo.setTextColor(Color.parseColor(Constants.COLOR_DARK_TEXTVIEW));
		}
		
		edPageUrlOrQuery.setText(pageUrlExtra);
		dialog.setView(view);
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int position) {
				String pageUrlOrQuery = edPageUrlOrQuery.getText().toString();
				dialogListener.onLoadPageButtonClick(dialogInterface, pageUrlOrQuery);
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
