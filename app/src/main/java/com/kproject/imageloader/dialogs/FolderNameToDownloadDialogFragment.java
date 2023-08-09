package com.kproject.imageloader.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.kproject.imageloader.R;
import com.kproject.imageloader.dialogs.FolderNameToDownloadDialogFragment;
import com.kproject.imageloader.models.Image;
import com.kproject.imageloader.services.DownloadService;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.Utils;
import java.io.File;
import java.util.ArrayList;

public class FolderNameToDownloadDialogFragment extends DialogFragment {
	private EditText edFolderName;
	private EditText edRangeStart;
	private EditText edRangeEnd;
	private TextView tvDownloadInRangeMessage;
	private CheckBox cbDownloadInRange;
	private Button btOk;
	private ArrayList<Image> originalImageList = new ArrayList<>();
	
	public FolderNameToDownloadDialogFragment() {}
	
	public static FolderNameToDownloadDialogFragment newInstance(ArrayList<Image> imageList, ArrayList<Image> originalImageList) {
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList("imageList", imageList);
		bundle.putParcelableArrayList("originalImageList", originalImageList);
		FolderNameToDownloadDialogFragment dialog = new FolderNameToDownloadDialogFragment();
		dialog.setArguments(bundle);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final ArrayList<Image> imageList = getArguments().getParcelableArrayList("imageList");
		originalImageList = getArguments().getParcelableArrayList("originalImageList");
		final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), Utils.setThemeForDialog());
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_folder_name_to_download, null);
		TextView tvDownloadPath = view.findViewById(R.id.tvFolderNameToDownLoad_DownloadPath);
		tvDownloadInRangeMessage = view.findViewById(R.id.tvFolderNameToDownLoad_DownloadInRangeMessage);
		tvDownloadPath.setText(getResources().getString(R.string.textview_path) + " " + Utils.getDownloadPath() + "/");
		edFolderName = view.findViewById(R.id.edFolderNameToDownload_FolderName);
		edRangeStart = view.findViewById(R.id.edFolderNameToDownload_RangeStart);
		edRangeEnd = view.findViewById(R.id.edFolderNameToDownload_RangeEnd);
		cbDownloadInRange = view.findViewById(R.id.cbFolderNameToDownload_DownloadInRange);
		btOk = view.findViewById(R.id.btFolderNameToDownload_Ok);
		Button btCancel = view.findViewById(R.id.btFolderNameToDownload_Cancel);
		final LinearLayout llLayoutOfRange = view.findViewById(R.id.llFolderNameToDownload_LayoutOfRange);
		llLayoutOfRange.setVisibility(cbDownloadInRange.isChecked() ? View.VISIBLE : View.GONE);
		
		if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
			tvDownloadPath.setTextColor(Color.parseColor(Constants.COLOR_DARK_TEXTVIEW));
			tvDownloadInRangeMessage.setTextColor(Color.parseColor(Constants.COLOR_DARK_TEXTVIEW));
			cbDownloadInRange.setTextColor(Color.parseColor("#CCCCCC"));
		}
		
		edFolderName.addTextChangedListener(edFolderNameWatcher);
		edRangeStart.addTextChangedListener(edRangeStartWatcher);
		edRangeEnd.addTextChangedListener(edRangeEndWatcher);
		rangeMessage();
		
		if (edFolderName.getText().toString().isEmpty()) {
			btOk.setEnabled(false);
		}
		
		cbDownloadInRange.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				llLayoutOfRange.setVisibility(cbDownloadInRange.isChecked() ? View.VISIBLE : View.GONE);
				boolean rangeEmpty = edRangeStart.getText().toString().isEmpty() || edRangeEnd.getText().toString().isEmpty();
				if (rangeEmpty) {
					btOk.setEnabled(false);
				} else {
					btOk.setEnabled(true);
				}
			}
		});
		
		btOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String downloadPath = Utils.getDownloadPath() + "/" + edFolderName.getText().toString();
				new File(downloadPath).mkdirs();
				Intent intent = new Intent(getActivity(), DownloadService.class);
				intent.setAction(Constants.START_SERVICE);
				if (!cbDownloadInRange.isChecked()) {
					intent.putParcelableArrayListExtra("imageList", imageList);
				} else {
					int rangeStart = Integer.parseInt(edRangeStart.getText().toString());
					int rangeEnd = Integer.parseInt(edRangeEnd.getText().toString());
					intent.putParcelableArrayListExtra("imageList", imageListWithRange(rangeStart, rangeEnd));
				}
				intent.putExtra("downloadPath", downloadPath);
				getActivity().startService(intent);
				FolderNameToDownloadDialogFragment.this.dismiss();
			}
		});
		
		btCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				FolderNameToDownloadDialogFragment.this.dismiss();
			}
		});
		
		dialog.setTitle(getResources().getString(R.string.dialog_download_images));
		dialog.setView(view);
		return dialog.create();
	}
	
	private TextWatcher edFolderNameWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {}

		@Override
		public void onTextChanged(CharSequence charSequence, int p2, int p3, int p4) {
			String folderNameReplaced = charSequence.toString().replaceAll("[^a-zA-Z0-9-_.\\s]", "-");
			boolean rangeEmpty = false;
			boolean rangeWrong = false;
			if (cbDownloadInRange.isChecked()) {
				rangeEmpty = edRangeStart.getText().toString().isEmpty() || edRangeEnd.getText().toString().isEmpty();
				int start = 0, end = 0;
				if (!rangeEmpty) {
					start = Integer.parseInt(edRangeStart.getText().toString());
					end = Integer.parseInt(edRangeEnd.getText().toString());
				}
				rangeWrong = !((start > 0) && (end <= originalImageList.size()) && (end > start));
			}
			if (!folderNameReplaced.equals(charSequence.toString()) || charSequence.toString().isEmpty() || rangeEmpty || rangeWrong) {
				btOk.setEnabled(false);
			} else {
				btOk.setEnabled(true);
			}
		}

		@Override
		public void afterTextChanged(Editable p1) {}

	};
	
	private TextWatcher edRangeStartWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {}

		@Override
		public void onTextChanged(CharSequence charSequence, int p2, int p3, int p4) {
			if (!charSequence.toString().isEmpty()) {
				int start = Integer.parseInt(charSequence.toString());
				int end = 0;
				if (!edRangeEnd.getText().toString().isEmpty()) {
					end = Integer.parseInt(edRangeEnd.getText().toString());
				}
				boolean condition = !edFolderName.getText().toString().isEmpty() && ((start > 0) && (end <= originalImageList.size()) && (end > start));
				btOk.setEnabled(condition ? true : false);
			} else {
				btOk.setEnabled(false);
			}
			rangeMessage();
		}

		@Override
		public void afterTextChanged(Editable p1) {}
		
	};
	
	private TextWatcher edRangeEndWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {}

		@Override
		public void onTextChanged(CharSequence charSequence, int p2, int p3, int p4) {
			if (!charSequence.toString().isEmpty()) {
				int start = 0;
				if (!edRangeStart.getText().toString().isEmpty()) {
					start = Integer.parseInt(edRangeStart.getText().toString());
				}
				int end = Integer.parseInt(charSequence.toString());
				boolean condition = !edFolderName.getText().toString().isEmpty() && ((start > 0) && (end <= originalImageList.size()) && (end > start));
				btOk.setEnabled(condition ? true : false);
			} else {
				btOk.setEnabled(false);
			}
			rangeMessage();
		}

		@Override
		public void afterTextChanged(Editable p1) {}

	};
	
	private void rangeMessage() {
		String imageTotalSize = originalImageList.size() + "";
		String rangeStart = edRangeStart.getText().toString();
		String rangeEnd = edRangeEnd.getText().toString();
		String str = getResources().getString(R.string.textview_download_in_range, imageTotalSize, (rangeStart.isEmpty() ? "??" : rangeStart), (rangeEnd.isEmpty() ? "??" : rangeEnd));
		Spanned strSpanned;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			strSpanned = Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY);
		} else {
			strSpanned = Html.fromHtml(str);
		}
		tvDownloadInRangeMessage.setText(strSpanned);
	}
	
	private ArrayList<Image> imageListWithRange(int start, int end) {
		ArrayList<Image> imageListWithRange = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			imageListWithRange.add(originalImageList.get(i - 1));
		}
		return imageListWithRange;
	}
	
}
