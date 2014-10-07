package com.coinprism.model;

import com.coinprism.utils.SecurePreferences;
import com.coinprism.wallet.R;
import com.coinprism.wallet.WalletOverview;
import com.coinprism.wallet.fragment.BalanceTab;
import com.coinprism.wallet.fragment.SendTab;
import com.coinprism.wallet.fragment.TransactionsTab;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.MnemonicCode;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class WalletState
{
    private final static String seedKey = "wallet.seed";
    private static WalletState state;

    private final WalletConfiguration configuration;
    private final APIClient api;
    private AddressBalance walletData;
    private BalanceTab balanceTab;
    private SendTab sendTab;
    private TransactionsTab transactionsTab;

    public WalletState(WalletConfiguration configuration, APIClient api)
    {
        this.configuration = configuration;
        this.api = api;

        Timer updateTimer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                WalletState.this.triggerUpdate();
            }
        };

        updateTimer.schedule(task, 0, 60000);
    }

    public static WalletState getState()
    {
        if (state == null)
            state = initialize();

        return state;
    }

    private static WalletState initialize()
    {
        SecurePreferences preferences = new SecurePreferences(CoinprismWalletApplication.getContext());

        String seed = preferences.getString(seedKey, null);

        WalletConfiguration wallet;
        if (seed == null)
        {
            seed = WalletConfiguration.createWallet();

            SecurePreferences.Editor editor = preferences.edit();
            editor.putString(seedKey, seed);
            editor.commit();
        }

        wallet = new WalletConfiguration(seed, NetworkParameters.fromID(NetworkParameters.ID_MAINNET));

        try
        {
            MnemonicCode.INSTANCE = new MnemonicCode(CoinprismWalletApplication.getContext().getAssets()
                .open("bip39-wordlist.txt"), null);
        }
        catch (IOException exception)
        { }

        return new WalletState(
            wallet,
            new APIClient(CoinprismWalletApplication.getContext().getString(R.string.api_base_url)));
    }

    public void triggerUpdate()
    {
        BalanceLoader loader = new BalanceLoader(this);
        loader.execute(configuration.getAddress());
    }

    public void updateData(AddressBalance data)
    {
        if (data != null)
            this.walletData = data;

        this.balanceTab.updateWallet();
        this.sendTab.updateWallet();
    }

    public AddressBalance getBalance()
    {
        return this.walletData;
    }

    public WalletConfiguration getConfiguration()
    {
        return this.configuration;
    }

    public APIClient getAPIClient()
    {
        return this.api;
    }

    public BalanceTab getBalanceTab()
    {
        return balanceTab;
    }

    public void setBalanceTab(BalanceTab balanceTab)
    {
        this.balanceTab = balanceTab;
    }

    public SendTab getSendTab()
    {
        return sendTab;
    }

    public void setSendTab(SendTab sendTab)
    {
        this.sendTab = sendTab;
    }

    public TransactionsTab getTransactionsTab()
    {
        return transactionsTab;
    }

    public void setTransactionsTab(TransactionsTab transactionsTab)
    {
        this.transactionsTab = transactionsTab;
    }
}
