package com.example.coinkarasu.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.OnItemClickListener;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.format.PriceFormat;
import com.example.coinkarasu.format.TrendValueFormat;
import com.example.coinkarasu.utils.VolleyHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private OnItemClickListener listener;
    private ArrayList<Coin> coins;

    private HashMap<String, Integer> symbolIconResIdMap;
    private ImageLoader imageLoader;

    public RecyclerViewAdapter(Context context, ArrayList<Coin> coins) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.coins = coins;

        symbolIconResIdMap = buildIconResIdMap(context, coins);
        imageLoader = VolleyHelper.getInstance(context).getImageLoader();
    }

    private HashMap<String, Integer> buildIconResIdMap(Context context, List<Coin> coins) {
        HashMap<String, Integer> map = new HashMap<>();
        Resources resources = context.getResources();
        String packageName = context.getPackageName();

        for (Coin coin : coins) {
            if (coin.isSectionHeader()) {
                continue;
            }
            String name = "ic_coin_" + coin.getSymbol().toLowerCase();
            int resId = resources.getIdentifier(name, "raw", packageName);
            map.put(coin.getSymbol(), resId);
        }

        return map;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.recycler_row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Coin coin = coins.get(position);

        holder.symbol.setText(coin.getSymbol());
        holder.price.setText(new PriceFormat(coin.getToSymbol()).format(coin.getPrice() - coin.getPrevPrice()));
        holder.trend.setText(new TrendValueFormat().format(coin.getTrend()));

        holder.icon.setDefaultImageResId(symbolIconResIdMap.get(coin.getSymbol()));
        holder.icon.setImageUrl(coin.getLargeImageUrl(), imageLoader);

        holder.symbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(coin, view, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return coins.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        NetworkImageView icon;
        TextView symbol;
        TextView price;
        TextView trend;

        ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.icon);
            symbol = view.findViewById(R.id.symbol);
            price = view.findViewById(R.id.price);
            trend = view.findViewById(R.id.trend);
        }
    }
}
