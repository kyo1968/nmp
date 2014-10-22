package jp.nmp;

import java.io.File;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

/**
 * Application preferences.
 * 
 * @author kyo
 * @version 1.0
 */
public final class SettingsActivity extends PreferenceActivity { 
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.activity_pref);

		/* Disable external storage option if external storage isn't available */
		CheckBoxPreference extStorage = (CheckBoxPreference) findPreference(getString(R.string.pref_key_external_storage));
		File extDir = getExternalFilesDir(null); 
		if (extDir != null) {
			extStorage.setSummaryOn(getString(R.string.pref_summary_external_storage, extDir.getAbsolutePath())); 
		} else {
			extStorage.setChecked(false);
			extStorage.setEnabled(false);
		}
		File intDir = getFilesDir();
		if (intDir != null) {
			extStorage.setSummaryOff(getString(R.string.pref_summary_external_storage, intDir.getAbsolutePath()));
		}
		
		CheckBoxPreference copyPassword = (CheckBoxPreference) findPreference(getString(R.string.pref_key_copy_password));
		copyPassword.setSummary(getString(R.string.pref_summary_copy_password, copyPassword.isChecked()));
	}
}