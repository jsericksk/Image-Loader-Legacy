package com.kproject.imageloader.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.kproject.imageloader.fragments.SettingsFragment;
import com.kproject.imageloader.utils.Utils;

public class SettingsActivity extends AppCompatActivity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Utils.setThemeForActivity());
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()  
			.replace(android.R.id.content, SettingsFragment.newInstance())
			.commit();
		}
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
}
