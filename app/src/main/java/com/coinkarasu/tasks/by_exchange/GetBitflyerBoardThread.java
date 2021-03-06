package com.coinkarasu.tasks.by_exchange;

import android.content.Context;

import com.coinkarasu.api.bitflyer.Client;
import com.coinkarasu.api.bitflyer.data.Board;
import com.coinkarasu.tasks.CKThread;

import java.util.concurrent.CountDownLatch;

public class GetBitflyerBoardThread extends CKThread {
    private CountDownLatch latch;

    private Client client;
    private Board board;

    public GetBitflyerBoardThread(Context context) {
        this.latch = null;

        this.client = new Client(context);
    }

    @Override
    public void run() {
        super.run();

        board = client.getBoard();

        if (latch != null) {
            latch.countDown();
        }
    }

    public GetBitflyerBoardThread setLatch(CountDownLatch latch) {
        this.latch = latch;
        return this;
    }

    public Board getBoard() {
        return board;
    }
}
