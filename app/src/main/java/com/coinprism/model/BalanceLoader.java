package com.coinprism.model;

import android.os.AsyncTask;

public class BalanceLoader extends AsyncTask<String, Integer, AddressBalance>
{
    private AddressBalance addressBalance;
    private WalletState parent;

    public BalanceLoader(WalletState parent)
    {
        this.parent = parent;
    }

    @Override
    protected AddressBalance doInBackground(String... addresses)
    {
        try
        {
            return parent.getAPIClient().getAddressBalance(addresses[0]);
        }
        catch (Exception e)
        {
            this.notifyError();
            return null;
        }
    }

    @Override
    protected void onPostExecute(AddressBalance result)
    {
        super.onPostExecute(result);
        //Do anything with response..

        this.parent.updateData(result);
    }

    private void notifyError()
    {

    }

    public AddressBalance getAddressBalance()
    {
        return this.addressBalance;
    }
}
