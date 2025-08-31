package com.wit.paks.hangmangame.server.game;

import com.wit.paks.hangmangame.MessageProtocol;
import com.wit.paks.hangmangame.server.Player;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class GuesserPlayerThread extends PlayerThread {
    private final String phrase;
    private final ObserverPlayerThread observerPlayer;
    private static final Logger log = LogManager.getLogger(GuesserPlayerThread.class.getName());

    public GuesserPlayerThread(Player player, String phrase, CountDownLatch latch,
                               ObserverPlayerThread observerPlayer, Map<Integer, Integer> points) {
        super(player, latch, points);
        this.phrase = phrase;
        this.observerPlayer = observerPlayer;
    }

    @Override
    public void run() {
        PrintWriter output = player.getPlayerOutputStream();
        BufferedReader input = player.getPlayerInputStream();
        char[] phraseStatus = new char[phrase.length()];
        Arrays.fill(phraseStatus, '-');

        String playerAnswer;
        output.println(MessageProtocol.START_ROUND + "!" + new String(phraseStatus));

        int guessedLetters = 0;
        int mistakes = 0;
        while(mistakes < 7 && guessedLetters < phrase.length()){
            try {
                playerAnswer = input.readLine();
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
            if(playerAnswer.startsWith(MessageProtocol.GUESS_LETTER.toString())){
                char letter = playerAnswer.charAt(4);
                boolean isCorrectGuess = false;
                for(int i=0; i < phrase.length(); i++){
                    if(phrase.charAt(i) == letter){
                        phraseStatus[i] = letter;
                        isCorrectGuess = true;
                        guessedLetters++;
                    }
                }

                try {
                    if(isCorrectGuess) {
                        observerPlayer.getStatusMsgQueue().put(
                                "" + MessageProtocol.UPDATE_OBSERVER_STATUS + player + "," + new String(phraseStatus));
                        if(guessedLetters == phrase.length()){
                            observerPlayer.getStatusMsgQueue().put(MessageProtocol.PLAYER_FINISHED_ROUND + "ok");
                            output.println(MessageProtocol.GUESS_VERDICT + "ok," + new String(phraseStatus));
                        } else {
                            output.println(MessageProtocol.UPDATE_PHRASE_STATUS + new String(phraseStatus));
                        }
                    } else {
                        mistakes++;
                        observerPlayer.getStatusMsgQueue().put(
                                "" + MessageProtocol.UPDATE_OBSERVER_STATUS + player + ",#" + mistakes);
                        if(mistakes < 7) {
                            output.println("" + MessageProtocol.UPDATE_MISTAKES_STATUS + mistakes);
                        } else {
                            observerPlayer.getStatusMsgQueue().put(MessageProtocol.PLAYER_FINISHED_ROUND + "bad");
                            output.println(MessageProtocol.GUESS_VERDICT + "bad");
                        }
                    }
                } catch(InterruptedException e) {
                    log.error(e);
                    throw new RuntimeException(e);
                }
            }
        }
        addPoints(7 - mistakes);
        latch.countDown();

        while(true) {
            try {
                if(input.readLine().equals(MessageProtocol.CONFIRM_FINISHING_ROUND.toString())) break;
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
    }
}
