package com.coinprism.wallet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.math.BigDecimal;
import java.util.List;

public class UserPreferences extends PreferenceActivity
{
    public final static String defaultFeesKey = "default_fees";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
            new GeneralPreferences()).commit();
    }

    public static class GeneralPreferences extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                this.getActivity().getBaseContext());

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.user_settings);
            PreferenceManager.setDefaultValues(GeneralPreferences.this.getActivity(), R.xml.user_settings, false);

            EditTextPreference defaultFeeText = (EditTextPreference) findPreference(defaultFeesKey);
            String currentValue = sharedPrefs.getString(
                defaultFeesKey, getResources().getString(R.string.default_fees));

            //p.setText(currentValue);
            defaultFeeText.setSummary(currentValue + " BTC");

            String versionName = BuildConfig.VERSION_NAME;
            Preference versionPreference = (Preference) findPreference("version_number");
            versionPreference.setSummary(versionName);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            if ("default_fees".equals(key))
            {
                EditTextPreference textPreference = (EditTextPreference) findPreference(key);
                String defaultFees = getResources().getString(R.string.default_fees);
                String value = sharedPreferences.getString(key, defaultFees);
                try
                {
                    BigDecimal decimal = new BigDecimal(value);

                    if (decimal.compareTo(BigDecimal.ZERO) >= 0)
                    {
                        textPreference.setSummary(defaultFees + " BTC");
                        return;
                    }
                }
                catch (RuntimeException e)
                { }

                textPreference.setText(value);
                textPreference.setSummary(value + " BTC");
            }
        }
    }
}
