package com.coinprism.wallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.coinprism.model.WalletState;
import com.google.common.base.Joiner;

import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;

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

            defaultFeeText.setSummary(getString(R.string.tab_wallet_bitcoin_count, currentValue));

            String versionName = BuildConfig.VERSION_NAME;
            Preference versionPreference = findPreference("version_number");
            versionPreference.setSummary(versionName);

            Preference seedBackupPreference = findPreference("backup_seed");
            seedBackupPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    try
                    {
                        final List<String> mnemonic = MnemonicCode.INSTANCE.toMnemonic(
                            WalletState.getState().getConfiguration().getSeed());

                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                            GeneralPreferences.this.getActivity());
                        final String fullMnemonic = Joiner.on(" ").join(mnemonic);
                        // Setting Dialog Title
                        alertDialog.setTitle(getString(R.string.settings_wallet_seed_title));
                        alertDialog.setMessage(
                            String.format(getString(R.string.settings_wallet_seed_message), fullMnemonic));

                        alertDialog.setPositiveButton(getString(android.R.string.copy), new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                ClipboardManager clipboard = (ClipboardManager) GeneralPreferences
                                    .this.getActivity().getSystemService(CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label", fullMnemonic);
                                clipboard.setPrimaryClip(clip);
                            }
                        });

                        alertDialog.setNegativeButton(
                            getString(R.string.settings_wallet_seed_close),
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.cancel();
                                }
                            });

                        alertDialog.show();
                        return true;
                    }
                    catch (MnemonicException.MnemonicLengthException exception)
                    { }

                    return false;
                }
            });
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
