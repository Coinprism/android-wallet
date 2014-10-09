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

import com.coinprism.model.SingleAssetTransaction;
import com.coinprism.model.TransactionsLoader;
import com.coinprism.model.WalletState;
import com.coinprism.wallet.R;
import com.coinprism.wallet.adapter.TransactionAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * The tab showing the list of recent transactions.
 */
public class TransactionsTab extends Fragment
{
    private TransactionAdapter adapter;
    private View loadingIndicator;
    private View errorMessageView;
    private ListView listView;
    private View listViewEmpty;

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
                final String url = getString(R.string.link_transaction, transaction.getTransactionId());
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        errorMessageView = rootView.findViewById(R.id.errorMessage);
        loadingIndicator = rootView.findViewById(R.id.loadingIndicator);
        listViewEmpty = rootView.findViewById(android.R.id.empty);

        WalletState.getState().setTransactionsTab(this);
        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        triggerRefresh();
    }

    /**
     * Updates the list of transactions.
     *
     * @param transactions the new list of transactions
     */
    public void updateTransactions(List<SingleAssetTransaction> transactions)
    {
        if (!isAdded())
            return;

        if (transactions != null)
        {
            this.adapter.clear();
            this.adapter.addAll(transactions);

            if (transactions.size() > 0)
            {
                listView.setVisibility(View.VISIBLE);
                listViewEmpty.setVisibility(View.GONE);
            }
            else
            {
                listView.setVisibility(View.GONE);
                listViewEmpty.setVisibility(View.VISIBLE);
            }
            loadingIndicator.setVisibility(View.GONE);
            errorMessageView.setVisibility(View.GONE);
        }
        else
        {
            listView.setVisibility(View.GONE);
            listViewEmpty.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.GONE);
            errorMessageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Triggers the asynchronous refresh of the recent transactions.
     */
    public void triggerRefresh()
    {
        if (loadingIndicator.getVisibility() == View.GONE)
        {
            listView.setVisibility(View.GONE);
            listViewEmpty.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.VISIBLE);
            errorMessageView.setVisibility(View.GONE);

            final TransactionsLoader loader = new TransactionsLoader(this);
            loader.execute(WalletState.getState().getConfiguration().getAddress());
        }
    }
}
