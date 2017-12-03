package com.example.toolbartest;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.toolbartest.coins.Coin;
import com.example.toolbartest.coins.CoinList;
import com.example.toolbartest.coins.CoinListImpl;
import com.example.toolbartest.coins.CoinListResponseImpl;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String MAIN_ACTIVITY_COIN_IDS_RESOURCE_NAME = "COIN_IDS_RESOURCE_NAME";
    public static final String COIN_ACTIVITY_COIN_NAME_KEY = "COIN_NAME_KEY";
    public static final String COIN_ACTIVITY_COIN_SYMBOL_KEY = "COIN_SYMBOL_KEY";

    private CoinList coinList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        coinList = null;
        String coinSymbolsResourceName = "default_watch_list_symbols";
        if (getIntent().getStringExtra(MainActivity.MAIN_ACTIVITY_COIN_IDS_RESOURCE_NAME) != null) {
            coinSymbolsResourceName = getIntent().getStringExtra(MainActivity.MAIN_ACTIVITY_COIN_IDS_RESOURCE_NAME);
        }

        if (CoinListResponseImpl.cacheExists(this)) {
            Log.d("CACHE", "Found");
            coinList = CoinListImpl.builder().setActivity(this).build();
            initializeCoinListView(coinSymbolsResourceName);
        } else {
            Log.d("CACHE", "Not found");
            final String finalCoinIdsResourceName = coinSymbolsResourceName;
            CoinListImpl.fetcher().setActivity(this).setListener(new CoinListImpl.Listener() {
                @Override
                public void finished(CoinList cl) {
                    cl.saveToFile(MainActivity.this);
                    MainActivity.this.coinList = cl;
                    initializeCoinListView(finalCoinIdsResourceName);
                }
            }).fetch();
        }
    }

    private String[] getStringResourceByName(String name) {
        Resources resources = getResources();
        int resId = resources.getIdentifier(name, "array", getPackageName());
        return resources.getStringArray(resId);
    }

    private void initializeCoinListView(String coinSymbolsResourceName) {
        ArrayList<Coin> coins = new ArrayList<>();
        String[] coinSymbols = getStringResourceByName(coinSymbolsResourceName);
        Log.d("Coin symbols", coinSymbols.toString());

        for (int i = 0; i < coinSymbols.length; i++) {
            Coin coin = coinList.getCoinBySymbol(coinSymbols[i]);
            if (coin == null) {
                Log.d("Coin not found", coinSymbols[i]);
            } else {
                coins.add(coin);
            }
        }

        ListView listView = findViewById(R.id.coin_list);
        listView.setAdapter(new CoinArrayAdapter(this, coins));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                ListView lv = (ListView) parent;
                Coin coin = (Coin) lv.getItemAtPosition(pos);

                if (pos == 0) {
                    Intent intent = new Intent(view.getContext(), CoinActivity.class);
                    intent.putExtra(COIN_ACTIVITY_COIN_NAME_KEY, coin.getCoinName());
                    intent.putExtra(COIN_ACTIVITY_COIN_SYMBOL_KEY, coin.getSymbol());
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, coin.getCoinName(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent(this, MainActivity.class);

        if (id == R.id.nav_camera) {
            intent.putExtra(MAIN_ACTIVITY_COIN_IDS_RESOURCE_NAME, "default_watch_list_symbols");
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {
            intent.putExtra(MAIN_ACTIVITY_COIN_IDS_RESOURCE_NAME, "jpy_toplist_symbols");
            startActivity(intent);
        } else if (id == R.id.nav_slideshow) {
            intent.putExtra(MAIN_ACTIVITY_COIN_IDS_RESOURCE_NAME, "usd_toplist_symbols");
            startActivity(intent);
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
