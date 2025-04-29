package com.wit.paks.hangmangame.server;

import com.wit.paks.hangmangame.server.game.MainGameThread;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

public class PlayersQueueThread extends Thread {
    private final ArrayBlockingQueue<Player> queue;
    private final boolean[] availableCodes;
    private static final int QUEUE_SIZE = 100;
    private static final int CODES_POOL_SIZE = 1000;
    private static final Logger log = LogManager.getLogger(PlayersQueueThread.class.getName());

    public PlayersQueueThread() {
        this.queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        this.availableCodes = new boolean[CODES_POOL_SIZE];
        Arrays.fill(availableCodes, true);
    }

    @Override
    public void run(){
        while(true){
            Player[] players = new Player[4];
            for(int i=0; i < players.length; i++){
                try {
                    players[i] = queue.take();
                } catch (InterruptedException e) {
                    log.warn(e, e);
                    return;
                }
            }
            new MainGameThread(players, this).start();
        }
    }

    public synchronized int getAvailableCode() {
        int j = 0;
        while(j < availableCodes.length && !availableCodes[j]) j++;
        if(j < availableCodes.length) {
            availableCodes[j] = false;
            return j + 1000;
        }
        return -1;
    }

    public synchronized void releaseCode(int code) {
        int index = code - 1000;
        if(index >= 0 && index < availableCodes.length)
            availableCodes[index] = true;
    }

    public void put(Player player) throws InterruptedException {
        queue.put(player);
    }
}
