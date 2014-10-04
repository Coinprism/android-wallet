package com.coinprism.model;

import com.coinprism.utils.SecurePreferences;
import com.coinprism.wallet.WalletOverview;
import com.coinprism.wallet.fragment.BalanceTab;
import com.coinprism.wallet.fragment.SendTab;
import com.coinprism.wallet.fragment.TransactionsTab;
import com.google.bitcoin.core.NetworkParameters;

import java.util.Timer;
import java.util.TimerTask;

public class WalletState
{
    private final static String privateKeyKey = "wallet.private_key";
    private final static String publicKeyKey = "wallet.public_key";
    private static WalletState state;

    private final SecurePreferences preferences;
    private final WalletConfiguration configuration;
    private final APIClient api;
    private AddressBalance walletData;
    private BalanceTab balanceTab;
    private SendTab sendTab;
    private TransactionsTab transactionsTab;

    public WalletState(WalletConfiguration configuration, APIClient api,
        SecurePreferences preferences)
    {
        this.configuration = configuration;
        this.api = api;
        this.preferences = preferences;

        Timer updateTimer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                WalletState.this.triggerUpdate();
            }
        };

        updateTimer.schedule(task, 0, 10000);
    }

    public static WalletState getState()
    {
        if (state == null)
            state = initialize();

        return state;
    }

    private static WalletState initialize()
    {
        SecurePreferences preferences =
            new SecurePreferences(CoinprismWalletApplication.getContext());

        String privateKey = preferences.getString(privateKeyKey, null);
        String publicKey;

        WalletConfiguration wallet;
        if (privateKey == null)
        {
            String[] key = WalletConfiguration.createWallet();

            SecurePreferences.Editor editor = preferences.edit();
            editor.putString(privateKeyKey, key[0]);
            editor.putString(publicKeyKey, key[1]);
            editor.commit();

            privateKey = key[0];
            publicKey = key[1];
        }
        else
        {
            publicKey = preferences.getString(publicKeyKey, null);
        }

        wallet = new WalletConfiguration(
            privateKey,
            publicKey,
            NetworkParameters.fromID(NetworkParameters.ID_TESTNET));

        return new WalletState(
            wallet,
            new APIClient("https://10.0.2.2:44300"),
            preferences);
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
