package com.kproject.imageloader.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import com.kproject.imageloader.R;
import com.kproject.imageloader.utils.Utils;

public class OpenSourceLicenseActivity extends AppCompatActivity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Utils.setThemeForActivity());
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source_license);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		WebView webView = findViewById(R.id.wvOpenSourceLicense_WebView);
		webView.loadUrl("file:///android_asset/license.html");
    }
	
}
