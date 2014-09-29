package com.coinprism.wallet.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.coinprism.model.AssetDefinition;
import com.coinprism.model.WalletState;
import com.coinprism.wallet.IUpdatable;
import com.coinprism.wallet.ProgressDialog;
import com.coinprism.wallet.R;
import com.coinprism.wallet.adapter.AssetSelectorAdapter;
import com.google.bitcoin.core.Transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

public class SendTab extends Fragment implements IUpdatable
{
    private AssetSelectorAdapter adapter;
    private Spinner assetSpinner;
    private EditText toAddress;
    private EditText amount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState)
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

        return rootView;
    }

    public void updateWallet()
    {
        this.adapter.clear();

        this.adapter.add(null);
        this.adapter.addAll(WalletState.getState().getAPIClient().getAllAssetDefinitions());
    }

    private void onSend()
    {
        final String to = toAddress.getText().toString();
        final String unitString = amount.getText().toString();
        final BigDecimal decimalAmount = new BigDecimal(unitString);

        final AssetDefinition selectedAsset = (AssetDefinition) assetSpinner.getSelectedItem();

        final ProgressDialog progressDialog = new ProgressDialog();

        AsyncTask<Void, Void, Transaction> getTransaction = new AsyncTask<Void, Void, Transaction>()
        {
            protected Transaction doInBackground(Void... _)
            {
                try
                {
                    if (selectedAsset == null)
                    {
                        return WalletState.getState().getAPIClient().buildBitcoinTransaction(
                            WalletState.getState().getConfiguration().getAddress(),
                            to, decimalAmount.scaleByPowerOfTen(8).toBigInteger().toString());
                    }
                    else
                    {
                        BigInteger unitAmount = decimalAmount
                            .scaleByPowerOfTen(selectedAsset.getDivisibility()).toBigInteger();
                        return WalletState.getState().getAPIClient().buildAssetTransaction(
                            WalletState.getState().getConfiguration().getAddress(),
                            to, unitAmount.toString(), selectedAsset.getAssetAddress());
                    }
                }
                catch (Exception e)
                {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Transaction result)
            {
                super.onPostExecute(result);
                progressDialog.dismiss();
                if (result != null)
                {
                    onConfirm(result);
                }
                else
                {
                    showError("An error occurred.");
                }
            }
        };

        progressDialog.configure("Please wait", "Verifying balance...");
        progressDialog.show(this.getActivity().getSupportFragmentManager(), "");

        getTransaction.execute();
    }

    private void onConfirm(final Transaction result)
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("Confirm transaction");
        alertDialog.setMessage("You are about to send X (Y) to the following address:\nabc");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

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

        AsyncTask<Void, Void, Boolean> getTransaction = new AsyncTask<Void, Void, Boolean>()
        {
            protected Boolean doInBackground(Void... addresses)
            {
                try
                {
                    WalletState.getState().getAPIClient().broadcastTransaction(result);
                    return true;
                }
                catch (Exception e)
                {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result)
            {
                super.onPostExecute(result);
                if (result)
                    progressDialog.dismiss();
                else
                    showError("The transaction could not be broadcasted.");
            }
        };

        progressDialog.configure("Please wait", "Broadcasting transaction...");
        progressDialog.show(this.getActivity().getSupportFragmentManager(), "");
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
