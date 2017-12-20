package com.example.coinkarasu.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.RelativeTimeSpanFragment;
import com.example.coinkarasu.coins.Coin;
import com.example.coinkarasu.format.PriceAnimator;
import com.example.coinkarasu.format.PriceBgColorAnimator;
import com.example.coinkarasu.format.PriceFormat;
import com.example.coinkarasu.format.TrendAnimator;
import com.example.coinkarasu.format.TrendColorFormat;
import com.example.coinkarasu.format.TrendIconFormat;
import com.example.coinkarasu.format.TrendValueFormat;
import com.example.coinkarasu.utils.PrefHelper;
import com.example.coinkarasu.utils.VolleyHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class ListViewAdapter extends BaseAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;

    private HashMap<String, Integer> symbolIconResIdMap;
    private ImageLoader imageLoader;
    private LayoutInflater inflater;
    private ArrayList<Coin> coins = new ArrayList<>();
    private TreeSet<Integer> sectionHeader = new TreeSet<>();
    private boolean isAnimEnabled;
    private boolean isDownloadIconEnabled;
    private boolean isScrolled;

    private PriceFormat priceFormatter;
    private TrendValueFormat trendFormatter;
    private TrendIconFormat trendIconFormat;
    private int trendUp;
    private int trendFlat;
    private int trendDown;
    private int priceUpFromColor;
    private int priceDownFromColor;
    private int priceToColor;
    private Typeface typeFace;
    private Typeface typeFaceItalic;

    private FragmentManager fragmentManager;

    public ListViewAdapter(Activity activity, List<Coin> coins, FragmentManager fragmentManager) {
        symbolIconResIdMap = buildIconResIdMap(activity, coins);
        imageLoader = VolleyHelper.getInstance(activity).getImageLoader();
        isAnimEnabled = PrefHelper.isAnimEnabled(activity);
        isDownloadIconEnabled = PrefHelper.isDownloadIconEnabled(activity);
        isScrolled = false;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (Coin coin : coins) {
            addItem(coin);
        }

        for (Coin coin : this.coins) {
            if (!coin.isSectionHeader()) {
                priceFormatter = new PriceFormat(coin.getToSymbol());
                break;
            }
        }
        trendFormatter = new TrendValueFormat();
        trendIconFormat = new TrendIconFormat();
        initializeColors(activity);
        typeFace = Typeface.createFromAsset(activity.getAssets(), "OpenSans-Light.ttf");
        typeFaceItalic = Typeface.createFromAsset(activity.getAssets(), "OpenSans-LightItalic.ttf");

        this.fragmentManager = fragmentManager;
    }

    private void initializeColors(Activity activity) {
        Resources resources = activity.getResources();
        TrendColorFormat formatter = new TrendColorFormat();

        trendUp = resources.getColor(formatter.format(1.0));
        trendFlat = resources.getColor(formatter.format(0.0));
        trendDown = resources.getColor(formatter.format(-1.0));

        priceUpFromColor = resources.getColor(R.color.colorPriceBgUp);
        priceDownFromColor = resources.getColor(R.color.colorPriceBgDown);
        priceToColor = Color.WHITE;
    }

    private HashMap<String, Integer> buildIconResIdMap(Activity activity, List<Coin> coins) {
        HashMap<String, Integer> map = new HashMap<>();
        Resources resources = activity.getResources();
        String packageName = activity.getPackageName();

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

    public void setIsScrolled(boolean flag) {
        this.isScrolled = flag;
    }

    public void setAnimEnabled(boolean flag) {
        this.isAnimEnabled = flag;
    }

    public void setToSymbol(String symbol) {
        for (Coin coin : coins) {
            coin.setToSymbol(symbol);
        }
        priceFormatter = new PriceFormat(symbol);
    }

    public void setDownloadIconEnabled(boolean flag) {
        this.isDownloadIconEnabled = flag;
    }

    public void addItem(Coin coin) {
        coins.add(coin);
        if (coin.isSectionHeader()) {
            sectionHeader.add(coins.size() - 1);
        }
        notifyDataSetChanged();
    }

    public void replaceItems(List<Coin> coins) {
        this.coins.clear();
        this.coins = new ArrayList<>();

        sectionHeader.clear();
        sectionHeader = new TreeSet<>();

        notifyDataSetChanged();

        for (Coin coin : coins) {
            addItem(coin);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return coins.size();
    }

    @Override
    public Coin getItem(int position) {
        return coins.get(position);
    }

    public ArrayList<Coin> getItems(String exchange) {
        ArrayList<Coin> filtered = new ArrayList<>();
        for (Coin coin : coins) {
            if (coin.isSectionHeader()) {
                continue;
            }
            if (coin.getExchange().equals(exchange)) {
                filtered.add(coin);
            }
        }

        return filtered;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int rowType = getItemViewType(position);
        Coin coin = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();

            if (rowType == TYPE_ITEM) {
                convertView = inflater.inflate(R.layout.list_row_item, parent, false);
                holder.icon = convertView.findViewById(R.id.coin_icon);
                holder.name = convertView.findViewById(R.id.coin_name);
                holder.symbol = convertView.findViewById(R.id.coin_symbol);
                holder.price = convertView.findViewById(R.id.coin_price);
                holder.trend = convertView.findViewById(R.id.coin_trend);
                holder.trendIcon = convertView.findViewById(R.id.coin_trend_icon);

                holder.name.setTypeface(typeFace);
                holder.symbol.setTypeface(typeFace);
            } else if (rowType == TYPE_HEADER) {
                convertView = inflater.inflate(R.layout.list_header_item, parent, false);
                holder.header = convertView.findViewById(R.id.text_separator);
                holder.divider = convertView.findViewById(R.id.divider);

                holder.header.setTypeface(typeFaceItalic);

                holder.progressbar = convertView.findViewById(R.id.progressbar);
                holder.progressbar.setTag(coin.getExchange() + "-progressbar");

                View timeSpan = convertView.findViewById(R.id.time_span_container);
                timeSpan.setId(Math.abs(coin.getExchange().hashCode()));
                fragmentManager.beginTransaction()
                        .replace(timeSpan.getId(), RelativeTimeSpanFragment.newInstance(), coin.getSymbol() + "-time_span")
                        .commit();
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (rowType == TYPE_ITEM) {
            holder.icon.setDefaultImageResId(symbolIconResIdMap.get(coin.getSymbol()));
            if (isDownloadIconEnabled) {
                holder.icon.setImageUrl(coin.getFullImageUrl(), imageLoader);
            } else {
                holder.icon.setImageUrl(null, imageLoader);
            }

            holder.name.setText(coin.getCoinName());
            holder.symbol.setText(coin.getSymbol());
            holder.trend.setTextColor(getTrendColor(coin.getTrend()));

            if (holder.priceAnimator != null) {
                holder.priceAnimator.cancel();
                holder.priceAnimator = null;
            }
            if (holder.priceBgColorAnimator != null) {
                holder.priceBgColorAnimator.cancel();
                holder.priceBgColorAnimator = null;
            }
            if (holder.trendAnimator != null) {
                holder.trendAnimator.cancel();
                holder.trendAnimator = null;
            }

            if (isAnimEnabled && !isScrolled) {
                if (coin.getPrice() != coin.getPrevPrice()) {
                    holder.priceAnimator = new PriceAnimator(coin, holder.price);
                    holder.priceAnimator.start();

                    if (coin.getPrice() > coin.getPrevPrice()) {
                        holder.priceBgColorAnimator = new PriceBgColorAnimator(priceUpFromColor, priceToColor, convertView);
                    } else {
                        holder.priceBgColorAnimator = new PriceBgColorAnimator(priceDownFromColor, priceToColor, convertView);
                    }
                    holder.priceBgColorAnimator.start();
                } else {
                    holder.price.setText(priceFormatter.format(coin.getPrice()));
                    convertView.setBackgroundColor(priceToColor);
                }

                if (coin.getTrend() != coin.getPrevTrend()) {
                    holder.trendAnimator = new TrendAnimator(coin, holder.trend);
                    holder.trendAnimator.start();
                } else {
                    holder.trend.setText(trendFormatter.format(coin.getTrend()));
                }
            } else {
                holder.price.setText(priceFormatter.format(coin.getPrice()));
                holder.trend.setText(trendFormatter.format(coin.getTrend()));
                convertView.setBackgroundColor(priceToColor);
            }

            holder.trendIcon.setImageResource(trendIconFormat.format(coin.getTrend()));
        } else if (rowType == TYPE_HEADER) {
            holder.header.setText(coin.getName());
            if (position == 0) {
                holder.divider.setVisibility(View.GONE);
            } else {
                holder.divider.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    private int getTrendColor(double trend) {
        int color;

        if (trend > 0.0) {
            color = trendUp;
        } else if (trend < 0.0) {
            color = trendDown;
        } else {
            color = trendFlat;
        }

        return color;
    }

    private static class ViewHolder {
        NetworkImageView icon;
        TextView name;
        TextView symbol;
        TextView price;
        TextView trend;
        ImageView trendIcon;

        TextView header;
        View divider;
        View progressbar;

        PriceAnimator priceAnimator = null;
        PriceBgColorAnimator priceBgColorAnimator = null;
        TrendAnimator trendAnimator = null;
    }
}
