package com.wit.paks.hangmangame.server;

import com.wit.paks.hangmangame.MessageProtocol;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerNameSettingTask extends Thread {
    private final Socket playerSocket;
    private final PlayersQueueThread playersQueue;
    private static final Logger log = LogManager.getLogger(PlayerNameSettingTask.class.getName());

    public PlayerNameSettingTask(Socket playerSocket, PlayersQueueThread playersQueue){
        this.playerSocket = playerSocket;
        this.playersQueue = playersQueue;
    }

    @Override
    public void run(){
        String playerName = null;
        try {
            PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
            do {
                out.println(MessageProtocol.SET_PLAYER_NAME);
                String playerAnswer = in.readLine().strip();
                if(MessageProtocol.getMessageProtocol(playerAnswer.substring(0, 4)) == MessageProtocol.SET_PLAYER_NAME)
                    playerName = playerAnswer.substring(4);
            } while(playerName == null);
        } catch (IOException e) {
            log.error(e);
            closeSocket();
        }

        int playerCode = playersQueue.getAvailableCode();

        try {
            PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
            Player player = new Player(playerName, playerCode, playerSocket);
            out.println("" + MessageProtocol.GET_PLAYER_NAME + player);
            playersQueue.put(player);
            out.println(MessageProtocol.PLACED_IN_QUEUE);
        } catch (InterruptedException e) {
            log.warn(e);
        } catch (IOException e) {
            log.error(e);
            closeSocket();
        }
    }

    private void closeSocket() {
        try {
            playerSocket.close();
        } catch (IOException e) {
            log.warn(e);
        }
    }
}
