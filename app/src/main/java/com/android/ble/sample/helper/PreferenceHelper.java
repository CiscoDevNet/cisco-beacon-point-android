package com.android.ble.sample.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.ble.sample.constants.PreferenceConstants;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by Entappiainc on 22-04-2016.
 */
public class PreferenceHelper {

    private SharedPreferences _sharedPrefs;
    private SharedPreferences.Editor _prefsEditor;
    private Context mContext;
    public PreferenceHelper(Context context) {
        this.mContext = context;
        this._sharedPrefs = context.getSharedPreferences(PreferenceConstants.PREFERENCE_NAME_MAIN, Activity.MODE_PRIVATE);
        this._prefsEditor = _sharedPrefs.edit();
    }

    public String getStringValue(String keyName) {
        return _sharedPrefs.getString(keyName, ""); // Get our string from prefs or return an empty string
    }

    public String getStringValue(String keyName, String defaultValue) {
        return _sharedPrefs.getString(keyName, defaultValue);
    }

    public void saveStringValue(String keyName, String text) {
        _prefsEditor.putString(keyName, text);
        _prefsEditor.commit();
    }

    public int getIntValue(String keyName) {
        return _sharedPrefs.getInt(keyName, 0); // Get our int from prefs or return an empty 0
    }

    public void saveIntValue(String keyName, int intValue) {
        _prefsEditor.putInt(keyName, intValue);
        _prefsEditor.commit();
    }

    public boolean getBooleanValue(String keyName) {
        return _sharedPrefs.getBoolean(keyName, false); // Get our boolean from prefs or return an false
    }

    public boolean getBooleanValue(String keyName, boolean defaultValue) {
        return _sharedPrefs.getBoolean(keyName, defaultValue); //  Get our boolean from prefs or return an @defaultValue
    }

    public void saveBooleanValue(String keyName, boolean text) {
        _prefsEditor.putBoolean(keyName, text);
        _prefsEditor.commit();
    }

    public Set<String> getStringSetValue(String keyName)
    {
        return _sharedPrefs.getStringSet(keyName, new HashSet<String>());
    }

    public void saveStringSetValue(String keyName, Set<String> text) {
        _prefsEditor.putStringSet(keyName, text);
        _prefsEditor.commit();
    }

    public float getFloatValue(String keyName) {
        return _sharedPrefs.getFloat(keyName, 0); // Get our Float from prefs or return an empty 0
    }

    public void saveFloatValue(String keyName, float floatValue) {
        _prefsEditor.putFloat(keyName, floatValue);
        _prefsEditor.commit();
    }

    public void clearPreference(String sPreferenceName)
    {
        SharedPreferences _clearSharedPrefs = mContext.getSharedPreferences(sPreferenceName, Activity.MODE_PRIVATE);
        SharedPreferences.Editor _clearPrefsEditor = _clearSharedPrefs.edit();
        _clearPrefsEditor.clear();
        _clearPrefsEditor.commit();
    }
}
