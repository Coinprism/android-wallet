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

package com.coinprism.model;

import android.os.AsyncTask;

import com.coinprism.wallet.fragment.TransactionsTab;

import java.util.List;

public class TransactionsLoader extends AsyncTask<String, Integer, List<SingleAssetTransaction>>
{
    private AddressBalance addressBalance;
    private TransactionsTab parent;

    public TransactionsLoader(TransactionsTab parent)
    {
        this.parent = parent;
    }

    @Override
    protected List<SingleAssetTransaction> doInBackground(String... addresses)
    {
        try
        {
            return WalletState.getState().getAPIClient().getTransactions(addresses[0]);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<SingleAssetTransaction> result)
    {
        super.onPostExecute(result);

        this.parent.updateTransactions(result);
    }
}
