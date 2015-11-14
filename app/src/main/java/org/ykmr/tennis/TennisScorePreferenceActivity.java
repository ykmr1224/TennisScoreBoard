package org.ykmr.tennis;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class TennisScorePreferenceActivity extends PreferenceActivity implements OnClickListener {
	public static final String EXTRA_PREFID = "EXTRA_PREFID";
	
	Map<String, Map<String, String>> prefs = new HashMap<String, Map<String, String>>();

	private void putPref(Resources res, String key, CharSequence[] values, CharSequence[] entries){
		HashMap<String, String> vals = new HashMap<String, String>();
		for(int i=0; i<values.length; i++) vals.put(values[i].toString(), entries[i].toString());
		prefs.put(key, vals);
		updateSummary(PreferenceManager.getDefaultSharedPreferences(this), key);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		addPreferencesFromResource(R.xml.pref2);
		
		Resources res = getResources();

		prefs.clear();
		
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		for(String key : prefs.getAll().keySet()){
			Preference p = getPreferenceManager().findPreference(key);
			if(p instanceof ListPreference){
				ListPreference l = (ListPreference)p;
				putPref(res, l.getKey(), l.getEntryValues(), l.getEntries());
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(listener);
	}
	
	private void updateSummary(SharedPreferences sharedPreferences, String key){
		Map<String, String> vals = prefs.get(key);
		if(vals != null){
			Preference p = findPreference(key);
			String val = sharedPreferences.getString(key, null);
			if(val != null){
				p.setSummary(vals.get(val));
			}
		}
	}

	// ここで summary を動的に変更
	private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(	SharedPreferences sharedPreferences, String key) {
			updateSummary(sharedPreferences, key);
		}
	};

	public void onClick(View v) {
		Intent i = new Intent();
		setResult(RESULT_OK, i);
		finish();
	}
}
