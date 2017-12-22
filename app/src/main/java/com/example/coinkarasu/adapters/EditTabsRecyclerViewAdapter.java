package com.example.coinkarasu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.coinkarasu.R;
import com.example.coinkarasu.activities.MainFragment.NavigationKind;
import com.example.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;

public class EditTabsRecyclerViewAdapter extends RecyclerView.Adapter<EditTabsRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private OnItemClickListener listener;
    private ArrayList<Item> items;

    private ResourceUtils resources;

    public EditTabsRecyclerViewAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        items = new ArrayList<>();

        for (NavigationKind kind : NavigationKind.values()) {
            if (kind == NavigationKind.edit_tabs) {
                continue;
            }
            String title = context.getResources().getString(kind.tabStrResId);
            items.add(new Item(title, "Summary", kind));
        }
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

        holder.title.setText(item.title);
        holder.summary.setText(item.summary);

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
                if (item.kind.isHideable()) {
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
        ImageView checkIcon;
        TextView title;
        TextView summary;

        ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
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
