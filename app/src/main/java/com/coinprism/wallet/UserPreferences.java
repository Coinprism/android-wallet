/*
 * Copyright (c) 2014 Flavien Charlon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.coinprism.wallet;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.coinprism.model.CoinprismWalletApplication;
import com.coinprism.model.WalletState;
import com.coinprism.utils.SecurePreferences;
import com.google.common.base.Joiner;

import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The activity for user preferences.
 */
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

    public class GeneralPreferences extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                this.getActivity().getBaseContext());

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.user_settings);
            PreferenceManager.setDefaultValues(GeneralPreferences.this.getActivity(), R.xml.user_settings, false);

            final EditTextPreference defaultFeeText = (EditTextPreference) findPreference(defaultFeesKey);
            final String currentValue = sharedPrefs.getString(
                defaultFeesKey, getResources().getString(R.string.default_fees));

            defaultFeeText.setSummary(getString(R.string.tab_wallet_bitcoin_count, currentValue));

            final String versionName = BuildConfig.VERSION_NAME;
            final Preference versionPreference = findPreference("version_number");
            versionPreference.setSummary(versionName);

            final Preference seedBackupPreference = findPreference("backup_seed");
            seedBackupPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                // Show the mnemonic for the wallet seed
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

                        alertDialog.setPositiveButton(getString(android.R.string.copy),
                            new DialogInterface.OnClickListener()
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

            final Preference seedRestorePreference = findPreference("restore_seed");
            seedRestorePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    // Display a dialog to restore the wallet seed
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        GeneralPreferences.this.getActivity());

                    final View dialogView = UserPreferences.this.getLayoutInflater().inflate(
                        R.layout.dialog_restore_seed, null);

                    alertDialog.setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                final EditText text = (EditText) dialogView.findViewById(R.id.mnemonicInput);
                                final String mnemonic = text.getText().toString();

                                String[] words = mnemonic.split(" ");
                                try
                                {
                                    byte[] mnemonicData = MnemonicCode.INSTANCE.toEntropy(Arrays.asList(words));

                                    SecurePreferences preferences = new SecurePreferences(CoinprismWalletApplication.getContext());

                                    SecurePreferences.Editor editor = preferences.edit();
                                    editor.putString(WalletState.seedKey, Base64.encodeToString(mnemonicData, Base64.DEFAULT));
                                    editor.commit();

                                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                                        GeneralPreferences.this.getActivity());
                                    alertDialog.setTitle(getString(R.string.settings_dialog_restore_seed_success_title));
                                    alertDialog.setMessage(getString(R.string.settings_dialog_restore_seed_success_message));
                                    alertDialog.setPositiveButton(getString(android.R.string.ok), null);
                                    alertDialog.show();

                                    return;
                                }
                                catch (MnemonicException.MnemonicLengthException e)
                                {
                                    e.printStackTrace();
                                }
                                catch (MnemonicException.MnemonicWordException e)
                                {
                                    e.printStackTrace();
                                }
                                catch (MnemonicException.MnemonicChecksumException e)
                                {
                                    e.printStackTrace();
                                }

                                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                                    GeneralPreferences.this.getActivity());
                                alertDialog.setTitle(getString(R.string.settings_dialog_restore_seed_error_title));
                                alertDialog.setMessage(getString(R.string.settings_dialog_restore_seed_error_message));
                                alertDialog.setPositiveButton(getString(android.R.string.ok), null);
                                alertDialog.show();
                            }
                        });

                    alertDialog.setNegativeButton(
                        getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.cancel();
                            }
                        });

                    alertDialog.setView(dialogView);
                    alertDialog.setTitle(getString(R.string.settings_dialog_restore_seed_title));
                    Dialog restoreDialog = alertDialog.create();
                    restoreDialog.show();

                    return true;
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
