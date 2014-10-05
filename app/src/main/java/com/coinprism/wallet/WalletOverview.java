package com.coinprism.wallet;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.coinprism.model.WalletState;
import com.coinprism.wallet.adapter.TabsPagerAdapter;
import com.google.common.base.Joiner;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;

import java.io.IOException;
import java.util.List;


public class WalletOverview extends FragmentActivity implements ActionBar.TabListener
{
    private ViewPager viewPager;
    private TabsPagerAdapter tabsPagerAdapter;
    private ActionBar actionBar;
    private String[] tabs = {"Send", "Wallet", "Transactions"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_overview);

        // Initialization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();

        tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(tabsPagerAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tabs
        for (String tab_name : tabs)
        {
            actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
        }

        // On swiping the viewpager make respective tab selected
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2)
            {
            }

            @Override
            public void onPageScrollStateChanged(int arg0)
            {
            }
        });

        viewPager.setCurrentItem(1);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        WalletState.getState().triggerUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wallet_overview, menu);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null)
        {
            WalletState.getState().getSendTab().setAddress(scanResult.getContents());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(this, UserPreferences.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_refresh)
        {
            WalletState.getState().getBalanceTab().triggerRefresh();
            WalletState.getState().getTransactionsTab().triggerRefresh();
            return true;
        }
        else if (id == R.id.export_seed)
        {
            try
            {
                final List<String> mnemonic = MnemonicCode.INSTANCE.toMnemonic(
                    WalletState.getState().getConfiguration().getSeed());

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                final String fullMnemonic = Joiner.on(" ").join(mnemonic);
                // Setting Dialog Title
                alertDialog.setTitle("Wallet seed");
                alertDialog.setMessage(
                    String.format("Your wallet seed encoded in mnemonic form is:\n\n%s", fullMnemonic));

                alertDialog.setPositiveButton("Copy", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", fullMnemonic);
                        clipboard.setPrimaryClip(clip);
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
                return true;
            }
            catch (MnemonicException.MnemonicLengthException exception)
            { }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft)
    {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft)
    {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft)
    {
    }
}
