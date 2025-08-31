package com.wit.paks.hangmangame.server.game;

import com.wit.paks.hangmangame.MessageProtocol;
import com.wit.paks.hangmangame.server.Player;
import com.wit.paks.hangmangame.server.PlayersQueueThread;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainGameThread extends Thread {
    private final Player[] players;
    private final PlayersQueueThread playersQueue;
    private final Map<Integer, String> playersPhrasesMap;
    private final Map<Integer, Integer> playersPointsMap;
    private static final Logger log = LogManager.getLogger(MainGameThread.class.getName());

    public MainGameThread(Player[] players, PlayersQueueThread playersQueue){
        this.players = players;
        this.playersQueue = playersQueue;
        this.playersPhrasesMap = new HashMap<>();
        this.playersPointsMap = new HashMap<>();
    }

    @Override
    public void run(){
        CountDownLatch countDownLatch = new CountDownLatch(4);
        for(Player player : players){
            new PlayerPhraseGettingTask(player, playersPhrasesMap, countDownLatch).start();
            playersPointsMap.put(player.getCode(), 0);
        }

        boolean wasPhrasesGotten;
        try {
            wasPhrasesGotten = countDownLatch.await(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.warn(e);
            wasPhrasesGotten = false;
        }
        if(!wasPhrasesGotten) {
            log.error("Not every phrase was gotten properly. Cannot continue the game.");
            return;
        }

        for(int i=0; i < players.length; i++){
            countDownLatch = new CountDownLatch(4);
            String roundPhrase = playersPhrasesMap.get(players[i].getCode());
            ObserverPlayerThread observerPlayer = new ObserverPlayerThread(players[i], countDownLatch, playersPointsMap);
            GuesserPlayerThread[] guesserPlayers = new GuesserPlayerThread[3];

            StringJoiner playersNamesMsg = new StringJoiner(",");
            for(int j=0, k=0; j < players.length; j++){
                if(j == i) continue;
                playersNamesMsg.add(players[j].toString());
                guesserPlayers[k++] = new GuesserPlayerThread(players[j], roundPhrase, countDownLatch,
                        observerPlayer, playersPointsMap);
            }

            try {
                playersNamesMsg.add(Integer.valueOf(roundPhrase.length()).toString());
                observerPlayer.getStatusMsgQueue().put(playersNamesMsg.toString());
            } catch (InterruptedException e) {
                log.error(e, e);
                return;
            }

            observerPlayer.start();
            for(GuesserPlayerThread guesserPlayer : guesserPlayers)
                guesserPlayer.start();

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error(e, e);
                return;
            }

            try {
                observerPlayer.join(60000);
                for(GuesserPlayerThread guesserPlayer : guesserPlayers)
                    guesserPlayer.join(60000);
            } catch (InterruptedException e) {
                log.error(e, e);
                return;
            }
        }

        String[] playersPoints = new String[4];
        int i = 0;
        for(Map.Entry<Integer, Integer> player : playersPointsMap.entrySet()){
            Player playerObj = null;
            for(Player plr : this.players)
                if(plr.getCode() == player.getKey()) playerObj = plr;
            playersPoints[i] = playerObj + ":" + player.getValue();
            i++;
        }
        Arrays.sort(playersPoints, (a , b) -> {
           int pointsA = Integer.parseInt(a.split(":")[1]);
           int pointsB = Integer.parseInt(b.split(":")[1]);
           return pointsB - pointsA;
        });
        StringJoiner playersRankingMessageJoiner = new StringJoiner(",");
        for(String p : playersPoints)
            playersRankingMessageJoiner.add(p);
        String playersRankingMessageString = MessageProtocol.FINISH_GAME + playersRankingMessageJoiner.toString();

        for(Player player : players) {
            new PlayerEndGameThread(player, playersQueue).start();
            player.getPlayerOutputStream().println(playersRankingMessageString);
        }
    }
}
