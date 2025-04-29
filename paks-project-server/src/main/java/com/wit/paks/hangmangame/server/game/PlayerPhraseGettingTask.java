package com.wit.paks.hangmangame.server.game;

import com.wit.paks.hangmangame.MessageProtocol;
import com.wit.paks.hangmangame.server.Player;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class PlayerPhraseGettingTask extends Thread {
    private final Player player;
    private final Map<Integer, String> phrasesMap;
    private final CountDownLatch latch;
    private static final Logger log = LogManager.getLogger(PlayerPhraseGettingTask.class.getName());

    public PlayerPhraseGettingTask(Player player, Map<Integer, String> phrasesMap, CountDownLatch latch){
        this.player = player;
        this.phrasesMap = phrasesMap;
        this.latch = latch;
    }

    @Override
    public void run() {
        player.getPlayerOutputStream().println(MessageProtocol.INPUT_PHRASE);
        try {
            String phrase;
            while(true) {
                String playerAnswer = player.getPlayerInputStream().readLine().strip();
                if(MessageProtocol.getMessageProtocol(playerAnswer.substring(0, 4)) == MessageProtocol.INPUT_PHRASE) {
                    phrase = playerAnswer.substring(4);
                    break;
                }
            }
            synchronized (phrasesMap) {
                phrasesMap.put(player.getCode(), phrase);
            }
            player.getPlayerOutputStream().println(MessageProtocol.WAIT_FOR_OTHERS);
            latch.countDown();
        } catch (IOException e) {
            log.error(e);
        }
    }
}
