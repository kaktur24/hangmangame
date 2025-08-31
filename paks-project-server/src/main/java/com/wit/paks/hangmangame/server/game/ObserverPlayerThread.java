package com.wit.paks.hangmangame.server.game;

import com.wit.paks.hangmangame.MessageProtocol;
import com.wit.paks.hangmangame.server.Player;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class ObserverPlayerThread extends PlayerThread {
    private final ArrayBlockingQueue<String> statusMsgQueue;
    private static final Logger log = LogManager.getLogger(ObserverPlayerThread.class.getName());

    public ObserverPlayerThread(Player player, CountDownLatch latch, Map<Integer, Integer> points){
        super(player, latch, points);
        this.statusMsgQueue = new ArrayBlockingQueue<>(20);
    }

    @Override
    public void run() {
        String msg;
        try {
            msg = statusMsgQueue.take();
        } catch (InterruptedException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        PrintWriter output = player.getPlayerOutputStream();
        output.println(MessageProtocol.START_ROUND + msg);

        int points = 0;
        try {
            int counter = 3;
            do {
                msg = statusMsgQueue.take();
                if (msg.startsWith(MessageProtocol.UPDATE_OBSERVER_STATUS.toString()))
                    output.println(msg);
                else if (msg.startsWith(MessageProtocol.PLAYER_FINISHED_ROUND.toString())){
                    counter--;
                    if(msg.endsWith("bad"))
                        points += 2;
                }

            } while (counter > 0);
        } catch (InterruptedException e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        addPoints(points);
        latch.countDown();

        output.println(MessageProtocol.ALL_FINISHED_ROUND);
        BufferedReader input = player.getPlayerInputStream();
        while(true) {
            try {
                if(input.readLine().equals(MessageProtocol.CONFIRM_FINISHING_ROUND.toString())) break;
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
    }

    public ArrayBlockingQueue<String> getStatusMsgQueue() {
        return statusMsgQueue;
    }
}
