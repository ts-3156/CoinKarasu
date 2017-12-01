package com.example.toolbartest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class CoinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);

        Intent intent = getIntent();
        String message = intent.getStringExtra("MESSAGE");

        TextView textView = findViewById(R.id.coin_name);
        textView.setText(message);
    }
}
