package com.coinprism.wallet.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.coinprism.model.AssetBalance;
import com.coinprism.model.SingleAssetTransaction;
import com.coinprism.model.TransactionsLoader;
import com.coinprism.model.WalletState;
import com.coinprism.wallet.R;
import com.coinprism.wallet.adapter.TransactionAdapter;

import java.util.ArrayList;
import java.util.List;

public class TransactionsTab extends Fragment
{
    private TransactionAdapter adapter;
    private View loadingIndicator;
    private View errorMessageView;
    private ListView listView;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_tab_transactions, container, false);

        this.adapter = new TransactionAdapter(this.getActivity(),
            new ArrayList<SingleAssetTransaction>());

        listView = (ListView) rootView.findViewById(R.id.transactionList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                final SingleAssetTransaction transaction = adapter.getItem(position);
                final String url = String.format("https://www.coinprism.info/tx/%s",
                    transaction.getAsset().getAssetAddress());
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        errorMessageView = rootView.findViewById(R.id.errorMessage);
        loadingIndicator = rootView.findViewById(R.id.loadingIndicator);

        WalletState.getState().setTransactionsTab(this);
        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        triggerRefresh();
    }

    public void updateTransactions(List<SingleAssetTransaction> transactions)
    {
        if (!isAdded())
            return;

        if (transactions != null)
        {
            this.adapter.clear();
            this.adapter.addAll(transactions);

            listView.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.GONE);
            errorMessageView.setVisibility(View.GONE);
        }
        else
        {
            listView.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.GONE);
            errorMessageView.setVisibility(View.VISIBLE);
        }
    }

    public void triggerRefresh()
    {
        if (listView.getVisibility() == View.VISIBLE)
        {
            listView.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.VISIBLE);
            errorMessageView.setVisibility(View.GONE);

            final TransactionsLoader loader = new TransactionsLoader(this);
            loader.execute(WalletState.getState().getConfiguration().getAddress());
        }
    }
}
