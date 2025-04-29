package com.wit.paks.hangmangame.server.game;

import com.wit.paks.hangmangame.MessageProtocol;
import com.wit.paks.hangmangame.server.Player;
import com.wit.paks.hangmangame.server.PlayersQueueThread;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class PlayerEndGameThread extends Thread {
    private final Player player;
    private final PlayersQueueThread playersQueue;
    private static final Logger log = LogManager.getLogger(PlayerEndGameThread.class.getName());

    public PlayerEndGameThread(Player player, PlayersQueueThread playersQueue) {
        this.player = player;
        this.playersQueue = playersQueue;
    }

    @Override
    public void run() {
        BufferedReader input = player.getPlayerInputStream();
        PrintWriter output = player.getPlayerOutputStream();

        boolean incorrectAnswer;
        do {
            String playerAnswer;
            incorrectAnswer = false;
            try {
                playerAnswer = input.readLine();
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }

            MessageProtocol playerAnswerProtocol = MessageProtocol.getMessageProtocol(playerAnswer.substring(0, 4));
            if(playerAnswerProtocol == MessageProtocol.PLAY_NEXT_GAME){
                try {
                    playersQueue.put(player);
                } catch (InterruptedException e) {
                    log.error(e);
                    throw new RuntimeException(e);
                }
                output.println(MessageProtocol.PLACED_IN_QUEUE);
            } else if(playerAnswerProtocol == MessageProtocol.END_CONNECTION) {
                playersQueue.releaseCode(player.getCode());
                output.println(MessageProtocol.END_CONNECTION);
            } else incorrectAnswer = true;
        } while(incorrectAnswer);
    }
}
