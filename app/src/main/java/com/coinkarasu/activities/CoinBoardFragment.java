package com.coinkarasu.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coinkarasu.R;
import com.coinkarasu.api.bitflyer.data.Board;
import com.coinkarasu.tasks.GetBoardTask;
import com.coinkarasu.utils.CKLog;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.List;


public class CoinBoardFragment extends Fragment implements View.OnClickListener {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "CoinBoardFragment";

    private Button btn;
    private String kind;
    private LinearLayout columnAsk;
    private LinearLayout columnPrice;
    private LinearLayout columnBid;

    public CoinBoardFragment() {
    }

    public static CoinBoardFragment newInstance(String kind) {
        CoinBoardFragment fragment = new CoinBoardFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = getArguments().getString("kind");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin_board, container, false);

        columnAsk = view.findViewById(R.id.column_ask);
        columnPrice = view.findViewById(R.id.column_price);
        columnBid = view.findViewById(R.id.column_bid);

        btn = view.findViewById(R.id.board_order_book);
        btn.setOnClickListener(this);
        btn.setBackgroundColor(Color.LTGRAY);

        view.findViewById(R.id.board_time_and_sales).setOnClickListener(this);

        new GetBoardTask(getActivity()).setListener(new GetBoardTask.Listener() {
            @Override
            public void finished(Board board) {
                if (board != null && board.getAsks() != null && board.getBids() != null) {
                    updateView(board);
                }
            }
        }).execute();

        return view;
    }

    private View divider() {
        View divider = new View(getActivity());
        int margin = getResources().getDimensionPixelSize(R.dimen.board_divider_vertical_spacing);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);

        params.setMargins(0, margin, 0, margin);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(Color.parseColor("#999999"));

        return divider;
    }

    private TextView blankCell(LinearLayout.LayoutParams params) {
        TextView view = new TextView(getActivity());
        view.setLayoutParams(params);
        return view;
    }

    private TextView sizeCell(double size, int color, LinearLayout.LayoutParams params, DecimalFormat format) {
        TextView view = new TextView(getActivity());
        view.setText(format.format(size));
        view.setLayoutParams(params);
        view.setGravity(Gravity.CENTER);
        view.setTextColor(color);
        return view;
    }

    private void addOverRow(double over, LinearLayout ask, LinearLayout price, LinearLayout bid, LinearLayout.LayoutParams params) {
        DecimalFormat format = new DecimalFormat("#.#");
        format.setMaximumFractionDigits(0);

        TextView overCell = new TextView(getActivity());
        overCell.setText(format.format(over));
        overCell.setLayoutParams(params);
        overCell.setGravity(Gravity.CENTER);

        TextView centerCell = new TextView(getActivity());
        centerCell.setText("OVER");
        centerCell.setLayoutParams(params);
        centerCell.setGravity(Gravity.CENTER);

        ask.addView(overCell);
        price.addView(centerCell);
        bid.addView(blankCell(params));
    }

    private void addUnderRow(double under, LinearLayout ask, LinearLayout price, LinearLayout bid, LinearLayout.LayoutParams params) {
        DecimalFormat format = new DecimalFormat("#.#");
        format.setMaximumFractionDigits(0);

        TextView underView = new TextView(getActivity());
        underView.setText(format.format(under));
        underView.setLayoutParams(params);
        underView.setGravity(Gravity.CENTER);

        TextView centerCell = new TextView(getActivity());
        centerCell.setText("UNDER");
        centerCell.setLayoutParams(params);
        centerCell.setGravity(Gravity.CENTER);

        ask.addView(blankCell(params));
        price.addView(centerCell);
        bid.addView(underView);
    }

    public void updateView(Board board) {
        if (isDetached() || getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        DecimalFormat priceFormat = new DecimalFormat("#.#");
        priceFormat.setMaximumFractionDigits(0);

        DecimalFormat sizeFormat = new DecimalFormat("#.#");
        sizeFormat.setMinimumFractionDigits(8);

        List<Board.Row> rows = board.getRows(8, 8);

        addOverRow(board.getOver(8), columnAsk, columnPrice, columnBid, layoutParams);

        for (int i = 0; i < rows.size(); i++) {
//            CKLog.d("ROW", row.askSize + ", " + row.price + ", " + row.bidSize);

            Board.Row row = rows.get(i);
            int priceColor = Color.BLACK;

            TextView askView;
            TextView bidView;
            TextView priceView;

            if (row.askSize > 0) {
                askView = sizeCell(row.askSize, ColorTemplate.COLORFUL_COLORS[0], layoutParams, sizeFormat);
                priceColor = ColorTemplate.COLORFUL_COLORS[0];
            } else {
                askView = blankCell(layoutParams);
            }

            if (row.bidSize > 0) {
                bidView = sizeCell(row.bidSize, ColorTemplate.COLORFUL_COLORS[1], layoutParams, sizeFormat);
                priceColor = ColorTemplate.COLORFUL_COLORS[1];
            } else {
                bidView = blankCell(layoutParams);
            }

            priceView = sizeCell(row.price, priceColor, layoutParams, priceFormat);

            columnAsk.addView(askView);
            columnBid.addView(bidView);
            columnPrice.addView(priceView);

            columnAsk.addView(divider());
            columnPrice.addView(divider());
            columnBid.addView(divider());
        }

        addUnderRow(board.getUnder(8), columnAsk, columnPrice, columnBid, layoutParams);

    }

    @Override
    public void onClick(View view) {
        String next;

        switch (view.getId()) {
            case R.id.board_order_book:
                next = "order_book";
                break;
            case R.id.board_time_and_sales:
                next = "time_and_sales";
                break;
            default:
                next = "order_book";
        }

        if (!next.equals(kind)) {
            Button nextBtn = (Button) view;
            btn.setBackgroundColor(Color.WHITE);
            nextBtn.setBackgroundColor(Color.LTGRAY);
            btn = nextBtn;

            kind = next;
        }

    }
}
