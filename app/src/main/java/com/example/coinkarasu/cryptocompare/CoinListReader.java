package com.example.coinkarasu.cryptocompare;

import android.app.Activity;
import android.util.Log;

import com.example.coinkarasu.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CoinListReader {
    public static String read(Activity activity) {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(
                    activity.getResources().openRawResource(R.raw.coin_list)));

            String buf;
            while ((buf = reader.readLine()) != null) {
                builder.append(buf);
            }
        } catch (IOException e) {
            Log.d("read", e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Log.d("read", e.getMessage());
            }
        }

        return builder.toString();
    }
}
