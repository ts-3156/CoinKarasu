package com.example.toolbartest.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.example.toolbartest.bitflyer.Client;
import com.example.toolbartest.bitflyer.data.Board;
import com.example.toolbartest.cryptocompare.data.History;

import java.util.ArrayList;

public class GetBoardTask extends AsyncTask<Integer, Integer, Integer> {
    Client client;
    Listener listener;
    Board board;

    public GetBoardTask(Activity activity) {
        client = new Client(activity);
        this.listener = null;
        this.board = null;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        board = client.getBoard();
        return 200;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null) {
            listener.finished(board);
        }
    }

    public GetBoardTask setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public interface Listener {
        void finished(Board board);
    }
}