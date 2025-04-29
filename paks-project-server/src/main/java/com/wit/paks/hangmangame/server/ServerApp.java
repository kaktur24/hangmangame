package com.wit.paks.hangmangame.server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    private static final int PORT_NUMBER = 32253;
    private static final Logger log = LogManager.getLogger(ServerApp.class.getName());

    static {
        DOMConfigurator.configure(ServerApp.class.getResource("/log4j.xml"));
    }

    public static void main(String[] args){
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            log.fatal(e, e);
            return;
        }

        PlayersQueueThread playersQueue = new PlayersQueueThread();
        playersQueue.start();

        while(true){
            try {
                Socket playerCon = serverSocket.accept();
                new PlayerNameSettingTask(playerCon, playersQueue).start();
            } catch (Exception e) {
                log.error(e, e);
                break;
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            log.error(e, e);
        }
    }
}
