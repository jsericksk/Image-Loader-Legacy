package com.kproject.imageloader.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.kproject.imageloader.R;
import com.kproject.imageloader.adapters.ManagerAdapter;
import com.kproject.imageloader.models.Manager;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FolderChooserDialogActivity extends AppCompatActivity implements ManagerAdapter.ItemClickListener {
	private static final String RESTORE_CURRENT_PATH = "restoreCurrentPath";
	private String homePath;
	
	private RecyclerView rvManagerList;
	private ManagerAdapter adpManagerAdapter;
	private TextView tvCurrentPath;
	private RelativeLayout rlMainLayout;
	private Button btCancel;
	private Button btSave;

	private List<Manager> folderList;
	private String currentPath;
	private File backDir;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(Utils.setThemeForDialog());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_activity_folder_chooser);
		this.setFinishOnTouchOutside(false);
		rvManagerList = findViewById(R.id.rvFolderChooser_ManagerList);
		tvCurrentPath = findViewById(R.id.tvFolderChooser_CurrentPath);
		btCancel = findViewById(R.id.btFolderChooser_Cancel);
		btSave = findViewById(R.id.btFolderChooser_Select);
		rlMainLayout = findViewById(R.id.rlFolderChooser_MainLayout);
		buttonClicks();
		
		homePath = Utils.getDownloadPath();
		
		if (savedInstanceState != null) {
			currentPath = savedInstanceState.getString(RESTORE_CURRENT_PATH);
			fileManager(currentPath);
		} else {
			fileManager(homePath);
		}
		
		setThemeColors();
	}
	
	private void setThemeColors() {
		if (Utils.getThemeSelected().equals(Constants.THEME_DARK)) {
			rlMainLayout.setBackgroundColor(Color.parseColor("#303030"));
			tvCurrentPath.setTextColor(Color.parseColor("#959595"));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(RESTORE_CURRENT_PATH, currentPath);
		super.onSaveInstanceState(outState);
	}

	private void buttonClicks() {
		btSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new File(currentPath).canWrite()) {
					Utils.setPreferenceValue(Constants.PREF_DOWNLOAD_PATH, currentPath);
					finish();
				} else {
					Utils.showToast(R.string.toast_folder_invalid);
				}
			}
		});

		btCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void fileManager(String folderPath) {
		tvCurrentPath.setText(getResources().getString(R.string.textview_path) +  " " + folderPath);
		folderList = new ArrayList<>();
		currentPath = folderPath;
		File path = new File(folderPath);
		if (path.getParentFile() != null) {
			backDir = path.getParentFile();
		}
		File[] files = path.listFiles();

		for (File file : files) {
			if (!file.isHidden() && file.canRead()) {
				if (file.isDirectory()) {
					folderList.add(new Manager(file.getName(), file.getPath()));
				}
			}
		}

		Collections.sort(folderList);
		
		if (canGoBackFolder()) {
			folderList.add(0, new Manager("...", backDir.getPath()));
		}

		adpManagerAdapter = new ManagerAdapter(this, folderList);
		rvManagerList.setLayoutManager(new LinearLayoutManager(this));
		adpManagerAdapter.setClickListener(this);
		rvManagerList.setAdapter(adpManagerAdapter);
	}

	@Override
	public void onRecyclerViewItemClick(View view, int position) {
		File file = new File(folderList.get(position).getFolderPath());
		if (file.isDirectory()) {
			fileManager(folderList.get(position).getFolderPath());
		}
	}

	@Override
	public void onBackPressed() {
		if (canGoBackFolder()) {
			fileManager(backDir.getPath());
		} else {
			super.onBackPressed();
		}
	}
	
	private boolean canGoBackFolder() {
		if (backDir != null) {
			return backDir.canRead() && !backDir.getPath().equals(currentPath);
		}
		return false;
	}
	
}
