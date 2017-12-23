package com.example.coinkarasu.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.NavigationKind;
import com.example.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;

public class EditTabsRecyclerViewAdapter extends RecyclerView.Adapter<EditTabsRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private OnItemClickListener listener;
    private ArrayList<Item> items;

    private SparseArray<Integer> colorResIdColorMap;

    public EditTabsRecyclerViewAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        items = new ArrayList<>();

        for (NavigationKind kind : NavigationKind.values()) {
            if (kind == NavigationKind.edit_tabs) {
                continue;
            }
            String title = context.getResources().getString(kind.titleStrResId);
            String summary = context.getResources().getString(kind.summaryStrResId);
            items.add(new Item(title, summary, kind));
        }

        colorResIdColorMap = buildColorResIdColorMap(context, NavigationKind.values());
    }

    private SparseArray<Integer> buildColorResIdColorMap(Context context, NavigationKind[] kinds) {
        SparseArray<Integer> map = new SparseArray<>();
        Resources resources = context.getResources();

        for (NavigationKind kind : kinds) {
            int resId = kind.colorResId;
            map.put(resId, resources.getColor(kind.colorResId));
        }

        return map;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.recycler_edit_tabs_row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Item item = items.get(position);

        holder.icon.setImageResource(item.kind.iconResId);
        holder.icon.clearColorFilter();
        if (!(item.kind == NavigationKind.bitflyer || item.kind == NavigationKind.coincheck || item.kind == NavigationKind.zaif)) {
            holder.icon.setColorFilter(colorResIdColorMap.get(item.kind.colorResId), PorterDuff.Mode.SRC_IN);
        }

        holder.title.setText(item.title);
        holder.summary.setText(Html.fromHtml(item.summary));

        if (position == 0) {
            holder.checkIcon.setImageResource(R.drawable.ic_edit_tabs_push_pin);
        } else {
            boolean isVisible = PrefHelper.isVisibleTab(context, item.kind);

            if (isVisible) {
                holder.checkIcon.setImageResource(R.drawable.ic_edit_tabs_check_green);
            } else {
                holder.checkIcon.setImageResource(R.drawable.ic_edit_tabs_check_white);
            }
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.kind.isHideable() && item.kind.isShowable()) {
                    listener.onItemClick(item, view, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View container;
        ImageView icon;
        ImageView checkIcon;
        TextView title;
        TextView summary;

        ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            icon = view.findViewById(R.id.icon);
            checkIcon = view.findViewById(R.id.icon_check);
            title = view.findViewById(R.id.title);
            summary = view.findViewById(R.id.summary);
        }
    }

    public static class Item {
        String title;
        String summary;
        public NavigationKind kind;

        Item(String title, String summary, NavigationKind kind) {
            this.title = title;
            this.summary = summary;
            this.kind = kind;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Item item, View view, int position);
    }

}
