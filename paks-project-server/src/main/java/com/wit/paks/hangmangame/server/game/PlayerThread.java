package com.wit.paks.hangmangame.server.game;

import com.wit.paks.hangmangame.server.Player;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public abstract class PlayerThread extends Thread {
    protected final Player player;
    protected final CountDownLatch latch;
    private final Map<Integer, Integer> points;

    public PlayerThread(Player player, CountDownLatch latch, Map<Integer, Integer> points){
        this.player = player;
        this.latch = latch;
        this.points = points;
    }

    public void addPoints(int pnt) {
        synchronized (this.points){
            Integer actualPoints = this.points.get(player.getCode());
            if(actualPoints == null) actualPoints = 0;
            this.points.put(player.getCode(), actualPoints + pnt);
        }
    }
    public abstract void run();
}
