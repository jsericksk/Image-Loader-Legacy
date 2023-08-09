package com.kproject.imageloader.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.kproject.imageloader.utils.Utils;

public abstract class BaseActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(Utils.setThemeForToolbar());
		super.onCreate(savedInstanceState);
	}
	
	public abstract void setThemeColors();
	
}
