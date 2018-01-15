package com.coinkarasu.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.coinkarasu.api.bitflyer.Client;
import com.coinkarasu.api.bitflyer.data.Board;

public class GetBoardTask extends AsyncTask<Integer, Integer, Board> {
    Client client;
    Listener listener;

    public GetBoardTask(Activity activity) {
        client = new Client(activity);
        this.listener = null;
    }

    @Override
    protected Board doInBackground(Integer... params) {
        return client.getBoard();
    }

    @Override
    protected void onPostExecute(Board board) {
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