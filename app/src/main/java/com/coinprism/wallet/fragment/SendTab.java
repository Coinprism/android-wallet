package com.coinprism.wallet.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import com.coinprism.model.APIException;
import com.coinprism.model.AssetDefinition;
import com.coinprism.model.WalletState;
import com.coinprism.utils.Formatting;
import com.coinprism.wallet.ProgressDialog;
import com.coinprism.wallet.R;
import com.coinprism.wallet.UserPreferences;
import com.coinprism.wallet.adapter.AssetSelectorAdapter;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.ScriptBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class SendTab extends Fragment
{
    private AssetSelectorAdapter adapter;
    private Spinner assetSpinner;
    private EditText toAddress;
    private EditText amount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_tab_send, container, false);
        this.assetSpinner = (Spinner) rootView.findViewById(R.id.assetSpinner);

        Button sendButton = (Button) rootView.findViewById(R.id.sendButton);
        toAddress = (EditText) rootView.findViewById(R.id.toAddress);
        amount = (EditText) rootView.findViewById(R.id.amount);

        sendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onSend();
            }
        });
        this.adapter = new AssetSelectorAdapter(this.getActivity(), new ArrayList<AssetDefinition>());
        this.adapter.add(null);
        this.assetSpinner.setAdapter(adapter);

        ImageButton scanButton = (ImageButton)rootView.findViewById(R.id.qrCodeButton);
        scanButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                IntentIntegrator integrator = new IntentIntegrator(SendTab.this.getActivity());
                integrator.initiateScan();
            }
        });

        WalletState.getState().setSendTab(this);
        return rootView;
    }

    public void updateWallet()
    {
        if (!isAdded())
            return;

        this.adapter.clear();

        this.adapter.add(null);
        this.adapter.addAll(WalletState.getState().getAPIClient().getAllAssetDefinitions());
    }

    public void setAddress(String value)
    {
        this.toAddress.setText(value);
    }

    private void onSend()
    {
        final String to = toAddress.getText().toString();
        final String unitString = amount.getText().toString();
        final BigDecimal decimalAmount;

        final AssetDefinition selectedAsset = (AssetDefinition) assetSpinner.getSelectedItem();

        final ProgressDialog progressDialog = new ProgressDialog();

        try
        {
            decimalAmount = new BigDecimal(unitString);
        }
        catch (NumberFormatException exception)
        {
            showError(getString(R.string.tab_send_error_invalid_amount));
            return;
        }

        AsyncTask<Void, Void, Transaction> getTransaction = new AsyncTask<Void, Void, Transaction>()
        {
            private String subCode;

            protected Transaction doInBackground(Void... _)
            {
                try
                {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                        SendTab.this.getActivity());

                    String value = sharedPreferences.getString(
                        UserPreferences.defaultFeesKey, getString(R.string.default_fees));

                    BigDecimal decimal = new BigDecimal(value);
                    long fees = decimal.scaleByPowerOfTen(8).toBigInteger().longValue();

                    if (selectedAsset == null)
                    {
                        return WalletState.getState().getAPIClient().buildTransaction(
                            WalletState.getState().getConfiguration().getAddress(),
                            to, decimalAmount.scaleByPowerOfTen(8).toBigInteger().toString(), null, fees);
                    }
                    else
                    {
                        BigInteger unitAmount = decimalAmount
                            .scaleByPowerOfTen(selectedAsset.getDivisibility()).toBigInteger();
                        return WalletState.getState().getAPIClient().buildTransaction(
                            WalletState.getState().getConfiguration().getAddress(),
                            to, unitAmount.toString(), selectedAsset.getAssetAddress(), fees);
                    }
                }
                catch (APIException exception)
                {
                    subCode = exception.getSubCode();
                    return null;
                }
                catch (Exception exception)
                {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Transaction result)
            {
                super.onPostExecute(result);

                if (!progressDialog.getIsCancelled())
                {
                    progressDialog.dismiss();
                    if (result != null)
                        onConfirm(result, decimalAmount, selectedAsset, to);
                    else if (subCode == null)
                        showError(getString(R.string.tab_send_error_connection_error));
                    else if (subCode.equals("InsufficientFunds"))
                        showError(getString(R.string.tab_send_error_insufficient_funds));
                    else if (subCode.equals("InsufficientColoredFunds"))
                        showError(getString(R.string.tab_send_error_insufficient_asset));
                    else if (subCode.equals("AmountUnderDustThreshold") || subCode.equals("ChangeUnderDustThreshold"))
                        showError(getString(R.string.tab_send_error_amount_too_low));
                    else
                        showError(getString(R.string.tab_send_error_server_error));
                }
            }
        };

        progressDialog.configure(
            getString(R.string.tab_send_dialog_please_wait),
            getString(R.string.tab_send_dialog_verifying_balance),
            true);
        progressDialog.show(this.getActivity().getSupportFragmentManager(), "");

        getTransaction.execute();
    }

    private void onConfirm(final Transaction result, final BigDecimal decimalAmount,
        final AssetDefinition selectedAsset, final String to)
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getActivity());

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.tab_send_dialog_confirm_transaction));

        final String assetName;
        if (selectedAsset == null)
            assetName = getString(R.string.tab_send_dialog_confirm_message_amount_bitcoin);
        else
        {
            if (selectedAsset.getName() != null && selectedAsset.getTicker() != null)
            {
                assetName = String.format(getString(R.string.tab_send_dialog_confirm_message_amount_known_asset),
                    selectedAsset.getTicker(), selectedAsset.getName());
            }
            else
            {
                assetName = String.format(
                    getString(R.string.tab_send_dialog_confirm_message_amount_unknown_asset),
                    selectedAsset.getAssetAddress());
            }
        }

        String message = String.format(getString(R.string.tab_send_dialog_confirm_message),
            Formatting.formatNumber(decimalAmount), assetName, to);

        alertDialog.setMessage(message);

        alertDialog.setPositiveButton(
            getString(R.string.tab_send_dialog_confirm_button),
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    onConfirmed(result);
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

        alertDialog.show();
    }

    private void onConfirmed(final Transaction result)
    {
        final ProgressDialog progressDialog = new ProgressDialog();

        AsyncTask<Void, Void, String> broadcastTask = new AsyncTask<Void, Void, String>()
        {
            protected String doInBackground(Void... addresses)
            {
                try
                {
                    for (int i = 0; i < result.getInputs().size(); i++)
                    {
                        ECKey key = WalletState.getState().getConfiguration().getKey();
                        TransactionSignature signature =
                            result.calculateSignature(i, key,
                                result.getInputs().get(i).getScriptBytes(), Transaction.SigHash.ALL, false);

                        result.getInputs().get(i).setScriptSig(ScriptBuilder.createInputScript(signature, key));
                    }

                    return WalletState.getState().getAPIClient().broadcastTransaction(result);
                }
                catch (Exception e)
                {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result)
            {
                super.onPostExecute(result);
                progressDialog.dismiss();

                if (result != null)
                {
                    showSuccess(result);
                }
                else
                {
                    showError(getString(R.string.tab_send_error_broadcast));
                }
            }
        };

        progressDialog.configure(
            getString(R.string.tab_send_dialog_please_wait),
            getString(R.string.tab_send_dialog_broadcasting),
            false);

        progressDialog.show(this.getActivity().getSupportFragmentManager(), "");

        broadcastTask.execute();
    }

    private void showSuccess(final String transactionId)
    {
        WalletState.getState().getBalanceTab().triggerRefresh();
        WalletState.getState().getTransactionsTab().triggerRefresh();

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getActivity());

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.tab_send_dialog_transaction_success_title));
        alertDialog.setMessage(getString(R.string.tab_send_dialog_transaction_successful_message));

        alertDialog.setPositiveButton(
            getString(R.string.tab_send_dialog_transaction_successful_see),
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    String url = String.format(getString(R.string.link_transaction), transactionId);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            });

        alertDialog.setNegativeButton(
            getString(R.string.tab_send_dialog_transaction_successful_close),
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });

        alertDialog.show();
    }

    private void showError(String message)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getActivity());

        alertDialog.setTitle(getString(R.string.tab_send_error_dialog_title));
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(getString(android.R.string.ok), null);

        alertDialog.show();
    }
}
