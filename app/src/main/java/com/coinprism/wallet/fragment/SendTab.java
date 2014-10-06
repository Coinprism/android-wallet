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
            showError("The amount is invalid.");
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
                        UserPreferences.defaultFeesKey, getResources().getString(R.string.default_fees));

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
                    {
                        onConfirm(result, decimalAmount, selectedAsset, to);
                    }
                    else if (subCode.equals("InsufficientFunds"))
                    {
                        showError("You don't have enough bitcoins on your address. Make sure incoming transactions are confirmed.");
                    }
                    else if (subCode.equals("InsufficientColoredFunds"))
                    {
                        showError("You don't have enough assets on your address. Make sure incoming transactions are confirmed.");
                    }
                    else if (subCode.equals("AmountUnderDustThreshold") || subCode.equals("ChangeUnderDustThreshold"))
                    {
                        showError("The amount you entered is too low.");
                    }
                    else if (subCode != null)
                    {
                        showError("An error occurred. Please make sure the application is up to date.");
                    }
                    else
                    {
                        showError("An error occurred. Please make sure you have an internet connection.");
                    }
                }
            }
        };

        progressDialog.configure("Please wait", "Verifying balance...", true);
        progressDialog.show(this.getActivity().getSupportFragmentManager(), "");

        getTransaction.execute();
    }

    private void onConfirm(final Transaction result, final BigDecimal decimalAmount,
        final AssetDefinition selectedAsset, final String to)
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("Confirm transaction");

        final String assetName;
        if (selectedAsset == null)
            assetName = "BTC";
        else
        {
            if (selectedAsset.getName() != null && selectedAsset.getTicker() != null)
            {
                assetName = String.format("%s (%s)",
                    selectedAsset.getTicker(), selectedAsset.getName());
            }
            else
            {
                assetName = String.format("units (Asset ID: %s)", selectedAsset.getAssetAddress());
            }
        }

        String message = String.format("You are about to send %s %s to the following address:\n\n%s",
            Formatting.formatNumber(decimalAmount), assetName, to);

        alertDialog.setMessage(message);

        alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                onConfirmed(result);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
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
                    showError("The transaction could not be broadcasted.");
                }
            }
        };

        progressDialog.configure("Please wait", "Broadcasting transaction...", false);
        progressDialog.show(this.getActivity().getSupportFragmentManager(), "");

        broadcastTask.execute();
    }

    private void showSuccess(final String transactionId)
    {
        WalletState.getState().getBalanceTab().triggerRefresh();
        WalletState.getState().getTransactionsTab().triggerRefresh();

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("Transaction successful");
        alertDialog.setMessage("The transaction has been successfully pushed to the network.");

        alertDialog.setPositiveButton("See transaction", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                String url = String.format("https://www.coinprism.info/tx/%s", transactionId);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener()
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

        alertDialog.setTitle("Error");
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Ok", null);

        alertDialog.show();
    }
}
