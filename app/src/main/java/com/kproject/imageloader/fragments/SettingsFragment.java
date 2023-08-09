package com.kproject.imageloader.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import com.kproject.imageloader.R;
import com.kproject.imageloader.activities.FolderChooserDialogActivity;
import com.kproject.imageloader.activities.OpenSourceLicenseActivity;
import com.kproject.imageloader.dialogs.ColorPickerDialogFragment;
import com.kproject.imageloader.utils.Constants;
import com.kproject.imageloader.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String RESTORE_CONFIGURATION_CHANGED = "restoreConfigurationChanged";
	
	private boolean configurationChanged;
	
	public SettingsFragment() {}
	
	public static SettingsFragment newInstance() {
		return new SettingsFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			/* 
			* A SettingsActivity vai ser recriada ao mudar o tema.
			* O RESULT_OK deve ser entregue à MainActivity após isso,
			* e não pode ser feito no onSharedPreferenceChanged()
			*/
			configurationChanged = savedInstanceState.getBoolean(RESTORE_CONFIGURATION_CHANGED);
			if (configurationChanged) {
				getActivity().setResult(getActivity().RESULT_OK);
			}
		}
	}
	
	@Override
	public void onCreatePreferences(Bundle bundle, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
		preferenceOnClicks();
		getSummaries();
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	/*
	* Chamado em onDestroy() para onSharedPreferenceChanged() ser chamado ao
	* voltar da SelectItemActivity e atualizar os dados
	*/
	@Override
    public void onDestroy() {
		super.onDestroy();
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(RESTORE_CONFIGURATION_CHANGED, configurationChanged);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		if (!key.equals(Constants.PREF_APP_THEME)) {
			getActivity().setResult(getActivity().RESULT_OK);
		} else {
			configurationChanged = true;
			getActivity().recreate();
		}
		getSummaries();
	}
	
	private void setSummaryPreference(String prefKey, String prefDefaultValue) {
		Preference preference = findPreference(prefKey);
		preference.setSummary(preference.getSharedPreferences().getString(prefKey, prefDefaultValue));
	}

	private void getSummaries() {
		setSummaryPreference(Constants.PREF_DOWNLOAD_PATH, Environment.getExternalStorageDirectory().toString() + "/Image Loader");
	}
	
	private void preferenceOnClicks() {
		findPreference(Constants.PREF_APP_THEME).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ColorPickerDialogFragment dialog = ColorPickerDialogFragment.newInstance(Constants.COLOR_PICKER_APP_THEME);
				dialog.show(getFragmentManager(), dialog.getTag());
				return true;
			}
		});
		
		findPreference(Constants.PREF_BACKGROUND_COLOR).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ColorPickerDialogFragment dialog = ColorPickerDialogFragment.newInstance(Constants.COLOR_PICKER_BACKGROUND_COLOR);
				dialog.show(getFragmentManager(), dialog.getTag());
				return true;
			}
		});
		
		findPreference(Constants.PREF_NUMBER_OF_COLUMNS_IN_GRID).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				dialogNumberOfColumnsInGrid();
				return true;
			}
		});
		
		findPreference(Constants.PREF_EXIT_CONFIRMATION).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				dialogExitConfirmation();
				return true;
			}
		});
		
		findPreference(Constants.PREF_ZOOM_LEVEL).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				dialogZoomLevel();
				return true;
			}
		});
		
		findPreference(Constants.PREF_USER_AGENT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				dialogUserAgent();
				return true;
			}
		});
		
		findPreference(Constants.PREF_REQUEST_TIMEOUT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				dialogRequestTimeout();
				return true;
			}
		});
		
		
		findPreference(Constants.PREF_DOWNLOAD_PATH).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(getActivity(), FolderChooserDialogActivity.class));
				return true;
			}
		});
		
		findPreference(Constants.PREF_OPEN_SOURCE_LICENSE).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(getActivity(), OpenSourceLicenseActivity.class));
				return true;
			}
		});
	}
	
	private void dialogNumberOfColumnsInGrid() {
		final String[] options = getResources().getStringArray(R.array.number_of_columns_in_grid);
		int savedOption = Integer.parseInt(Utils.getPreferenceValue(Constants.PREF_NUMBER_OF_COLUMNS_IN_GRID, "2"));
		// Procura o índice do item de acordo com seu valor
		int indexOfSavedOption = new ArrayList<String>(Arrays.asList(options)).indexOf(String.valueOf(savedOption));
		final ArrayList<String> selectedOption = new ArrayList<>();
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), Utils.setThemeForDialog());
		dialog.setTitle(getResources().getString(R.string.preference_list_with_grid_columns));
		dialog.setSingleChoiceItems(options, indexOfSavedOption, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int pos) {
					selectedOption.add(0, options[pos]);
				}
			});
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int pos) {
					// Verificação para caso o usuário não tenha selecionado nenhuma opção
					if (!selectedOption.isEmpty()) {
						Utils.setPreferenceValue(Constants.PREF_NUMBER_OF_COLUMNS_IN_GRID, selectedOption.get(0));
					}
					dialogInterface.dismiss();
				}
			});
		dialog.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int pos) {
					dialogInterface.dismiss();
				}
			});
		dialog.show();
	}
	
	private void dialogExitConfirmation() {
		final String[] options = getResources().getStringArray(R.array.exit_confirmation);
		int savedOption = Integer.parseInt(Utils.getPreferenceValue(Constants.PREF_EXIT_CONFIRMATION, "0"));
		final ArrayList<String> selectedOption = new ArrayList<>();
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), Utils.setThemeForDialog());
		dialog.setTitle(getResources().getString(R.string.preference_exit_confirmation));
		dialog.setSingleChoiceItems(options, savedOption, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				selectedOption.add(0, String.valueOf(pos));
			}
		});
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				// Verificação para caso o usuário não tenha selecionado nenhuma opção
				if (!selectedOption.isEmpty()) {
					Utils.setPreferenceValue(Constants.PREF_EXIT_CONFIRMATION, selectedOption.get(0));
				}
				dialogInterface.dismiss();
			}
		});
		dialog.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				dialogInterface.dismiss();
			}
		});
		dialog.show();
	}
	
	private void dialogZoomLevel() {
		final String[] options = getResources().getStringArray(R.array.zoom_level);
		int savedOption = Integer.parseInt(Utils.getPreferenceValue(Constants.PREF_ZOOM_LEVEL, "3"));
		final ArrayList<String> selectedOption = new ArrayList<>();
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), Utils.setThemeForDialog());
		dialog.setTitle(getResources().getString(R.string.preference_zoom_level));
		dialog.setSingleChoiceItems(options, savedOption, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				selectedOption.add(0, String.valueOf(pos));
			}
		});
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				// Verificação para caso o usuário não tenha selecionado nenhuma opção
				if (!selectedOption.isEmpty()) {
					Utils.setPreferenceValue(Constants.PREF_ZOOM_LEVEL, selectedOption.get(0));
				}
				dialogInterface.dismiss();
			}
		});
		dialog.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				dialogInterface.dismiss();
			}
		});
		dialog.show();
	}
	
	private void dialogUserAgent() {
		final String[] options = getResources().getStringArray(R.array.user_agent);
		int savedOption = Integer.parseInt(Utils.getPreferenceValue(Constants.PREF_USER_AGENT, "2"));
		final ArrayList<String> selectedOption = new ArrayList<>();
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), Utils.setThemeForDialog());
		dialog.setTitle(getResources().getString(R.string.preference_user_agent));
		dialog.setSingleChoiceItems(options, savedOption, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				selectedOption.add(0, String.valueOf(pos));
			}
		});
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				// Verificação para caso o usuário não tenha selecionado nenhuma opção
				if (!selectedOption.isEmpty()) {
					Utils.setPreferenceValue(Constants.PREF_USER_AGENT, selectedOption.get(0));
				}
				dialogInterface.dismiss();
				}
			});
		dialog.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int pos) {
					dialogInterface.dismiss();
				}
			});
		dialog.show();
	}
	
	private void dialogRequestTimeout() {
		final String[] options = getResources().getStringArray(R.array.request_timeout);
		final String[] optionsInMilliSec = {"15000", "30000", "60000"};
		int savedOption = Integer.parseInt(Utils.getPreferenceValue(Constants.PREF_REQUEST_TIMEOUT, "15000"));
		// Procura o índice do item de acordo com seu valor
		int indexOfSavedOption = new ArrayList<String>(Arrays.asList(optionsInMilliSec)).indexOf(String.valueOf(savedOption));
		final ArrayList<String> selectedOption = new ArrayList<>();
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), Utils.setThemeForDialog());
		dialog.setTitle(getResources().getString(R.string.preference_request_timeout));
		dialog.setSingleChoiceItems(options, indexOfSavedOption, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				selectedOption.add(0, optionsInMilliSec[pos]);
			}
		});
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				// Verificação para caso o usuário não tenha selecionado nenhuma opção
				if (!selectedOption.isEmpty()) {
					Utils.setPreferenceValue(Constants.PREF_REQUEST_TIMEOUT, selectedOption.get(0));
				}
				dialogInterface.dismiss();
			}
		});
		dialog.setNegativeButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				dialogInterface.dismiss();
			}
		});
		dialog.show();
	}

}
