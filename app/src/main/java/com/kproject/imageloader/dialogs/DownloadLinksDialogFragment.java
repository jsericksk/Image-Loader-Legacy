package com.kproject.imageloader.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.kproject.imageloader.R;
import com.kproject.imageloader.models.Image;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.Utils;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class DownloadLinksDialogFragment extends DialogFragment {
	
	public DownloadLinksDialogFragment() {}
	
	public static DownloadLinksDialogFragment newInstance(ArrayList<Image> imageList) {
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList("imageList", imageList);
		DownloadLinksDialogFragment dialog = new DownloadLinksDialogFragment();
		dialog.setArguments(bundle);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final ArrayList<Image> imageList = getArguments().getParcelableArrayList("imageList");
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), Utils.setThemeForDialog());
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_download_links, null);
		TextView tvDownloadPath = view.findViewById(R.id.tvDownloadLinks_DownloadPath);
		
		if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
			tvDownloadPath.setTextColor(Color.parseColor(Constants.COLOR_DARK_TEXTVIEW));
		}
		
		tvDownloadPath.setText(getResources().getString(R.string.textview_path) + " " + Utils.getDownloadPath() + "/");
		final EditText edFileName = view.findViewById(R.id.edDownloadLinks_FileName);
		final RadioGroup rgFileOption = view.findViewById(R.id.rgDownloadLinks_FileOption);
		dialog.setTitle(getResources().getString(R.string.dialog_download_links));
		dialog.setView(view);
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				if (!edFileName.getText().toString().isEmpty()) {
					try {
						int fileOptionSelected = rgFileOption.getCheckedRadioButtonId();
						String fileOutputPath = Utils.getDownloadPath() + "/" + edFileName.getText().toString();
						String links = "";
						if (fileOptionSelected == R.id.rbDownloadLinks_JsonOption) {
							fileOutputPath += ".json";
							links = getLinks(imageList, 0);
						} else if (fileOptionSelected == R.id.rbDownloadLinks_TxtOption) {
							fileOutputPath += ".txt";
							links = getLinks(imageList, 1);
						}
						BufferedWriter writer = new BufferedWriter(new FileWriter(fileOutputPath));
						writer.write(links);
						writer.flush();
						writer.close();
						dialogDownloadSuccessfully(fileOutputPath);
					} catch (Exception e) {
						Utils.showToast(R.string.toast_download_links_error);
					}
				} else {
					Utils.showToast(R.string.toast_file_name_empty_error);
				}
			}
		});
		dialog.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				dialogInterface.dismiss();
			}
		});
		return dialog.create();
	}
	
	private String getLinks(ArrayList<Image> imageList, int fileOption) {
		// Verifica para inicializar como JSON ou não
		StringBuilder links = new StringBuilder(fileOption == 0 ? "{\"urls\":[" : "");
		int index = 1;
		for (Image image : imageList) {
			if (fileOption == 0) {
				String imageUrl = image.getImageUrl().replaceAll("/", "\\\\/");
				links.append("\"" + imageUrl + (index < imageList.size() ? "\", " : "\""));
				index++;
			} else {
				links.append(index + ". " + image.getImageUrl() + "\n\n");
				index++;
			}
		}
		links.append(fileOption == 0 ? "]}" : "");
		return links.toString();
	}
	
	private void dialogDownloadSuccessfully(String filePath) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setTitle(getResources().getString(R.string.dialog_links_downloaded_successfully));
		dialog.setMessage(getResources().getString(R.string.dialog_links_downloaded_successfully_message, filePath));
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				dialogInterface.dismiss();
			}
		});
		dialog.show();
	}
	
}
