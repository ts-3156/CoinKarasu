package com.coinkarasu.api.cryptocompare;

import android.content.Context;
import android.util.Log;

import com.coinkarasu.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CoinListReader {
    public static String read(Context context) {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(
                    context.getResources().openRawResource(R.raw.coin_list)));

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
