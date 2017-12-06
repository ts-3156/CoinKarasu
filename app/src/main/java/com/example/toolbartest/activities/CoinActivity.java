package com.example.toolbartest.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.toolbartest.R;

public class CoinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);

        Intent intent = getIntent();
        String coinName = intent.getStringExtra(MainActivity.COIN_ACTIVITY_COIN_NAME_KEY);
        String coinSymbol = intent.getStringExtra(MainActivity.COIN_ACTIVITY_COIN_SYMBOL_KEY);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(coinName);
        bar.setSubtitle(coinSymbol);

        TextView textView = findViewById(R.id.coin_name);
        textView.setText(coinName);
    }
}
