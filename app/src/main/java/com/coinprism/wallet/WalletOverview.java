package com.coinprism.wallet;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.view.ViewPager;

import com.coinprism.model.WalletState;
import com.coinprism.wallet.adapter.TabsPagerAdapter;

import java.util.Timer;
import java.util.TimerTask;


public class WalletOverview extends FragmentActivity implements ActionBar.TabListener, IUpdatable
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
            actionBar.addTab(
                    actionBar.newTab().setText(tab_name).setTabListener(this));
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

//        if (savedInstanceState == null)
//        {
//            getFragmentManager().beginTransaction()
//                    .add(R.id.container, new PlaceholderFragment())
//                    .commit();
//        }
    }

    public void updateWallet()
    {
        for (int i = 0; i < tabsPagerAdapter.getCount(); i++)
            ((IUpdatable) tabsPagerAdapter.getItem(i)).updateWallet();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        WalletState.getState().setCurrentActivity(this);
        WalletState.getState().triggerUpdate();

        Timer updateTimer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                WalletState.getState().triggerUpdate();
            }
        };

        updateTimer.schedule(task, 0, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wallet_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
            return true;

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
