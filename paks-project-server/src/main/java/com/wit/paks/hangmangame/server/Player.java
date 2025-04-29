package com.wit.paks.hangmangame.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Player {
    private final String name;
    private final int code;
    private final PrintWriter playerOutputStream;
    private final BufferedReader playerInputStream;

    public Player(String name, int code, Socket socket) throws IOException{
        this.name = name;
        this.code = code;
        this.playerOutputStream = new PrintWriter(socket.getOutputStream(), true);
        this.playerInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public String getName() {
        return name;
    }
    public int getCode() {
        return code;
    }
    public PrintWriter getPlayerOutputStream() {
        return playerOutputStream;
    }
    public BufferedReader getPlayerInputStream() {
        return playerInputStream;
    }

    public String toString() {
        return name + " (#" + code +")";
    }
}
