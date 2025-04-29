package com.wit.paks.hangmangame.client;

import com.wit.paks.hangmangame.MessageProtocol;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class GameController extends Thread{
    private final ClientAppWindow clientAppWindow;
    private final Socket serverConnection;
    private final BufferedReader input;
    private final PrintWriter output;
    private String userName;

    private static final String SERVER_HOSTNAME = "localhost";
    private static final int SERVER_PORT_NUMBER = 32253;

    public GameController(ClientAppWindow clientAppWindow, String userName) throws IOException {
        this.clientAppWindow = clientAppWindow;
        this.serverConnection = new Socket(SERVER_HOSTNAME, SERVER_PORT_NUMBER);
        this.input = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
        this.output = new PrintWriter(serverConnection.getOutputStream(), true);
        this.userName = userName;
    }

    @Override
    public void run() {
        boolean play = true;
        while(play) {
            String serverMsg;
            try {
                serverMsg = input.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            MessageProtocol serverMsgProtocol = MessageProtocol.getMessageProtocol(serverMsg.substring(0, 4));

            switch(serverMsgProtocol) {
                case SET_PLAYER_NAME : {
                    output.println("" + MessageProtocol.SET_PLAYER_NAME + userName);
                    break;
                }
                case GET_PLAYER_NAME: {
                    String userName = serverMsg.substring(4);
                    if(StringUtils.isNotBlank(userName))
                        this.userName = userName;
                    clientAppWindow.setUserName(this.userName);
                    break;
                }
                case PLACED_IN_QUEUE:
                case WAIT_FOR_OTHERS: {
                    clientAppWindow.loadContentPane(PanelFactory.getWaitingPanel());
                    break;
                }
                case INPUT_PHRASE: {
                    clientAppWindow.loadContentPane(PanelFactory.getPhraseInputPanel(output));
                    break;
                }
                case START_ROUND: {
                    String messageContent = serverMsg.substring(4);
                    if(messageContent.startsWith("!")){
                        playRound(messageContent.substring(1));
                    } else {
                        String[] msgConSplit = messageContent.split(",");
                        char[] startPhrase = new char[Integer.parseInt(msgConSplit[3])];
                        Arrays.fill(startPhrase, '-');
                        observeRound(new String(startPhrase),
                                new String[]{msgConSplit[0], msgConSplit[1], msgConSplit[2]});
                    }
                    break;
                }
                case FINISH_GAME: {
                    String messageContent = serverMsg.substring(4);
                    clientAppWindow.loadContentPane(PanelFactory.getRankingPanel(messageContent, output));
                    break;
                }
                case END_CONNECTION: {
                    play = false;
                    clientAppWindow.dispose();
                    try {
                        serverConnection.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void playRound(String startPhraseStatus) {
        PanelFactory.GuesserGamePanel guesserPanel = PanelFactory.getGuesserGamePanel(startPhraseStatus, output);
        clientAppWindow.loadContentPane(guesserPanel);

        boolean play = true;
        while(play) {
            String serverMsg;
            try {
                serverMsg = input.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            MessageProtocol serverMsgProtocol = MessageProtocol.getMessageProtocol(serverMsg.substring(0, 4));
            String msgContent;

            switch(serverMsgProtocol) {
                case GUESS_VERDICT: {
                    msgContent = serverMsg.substring(4);
                    if(msgContent.startsWith("ok")){
                        String phraseStatus = msgContent.split(",")[1];
                        guesserPanel.setPhraseStatus(phraseStatus);
                    } else if(msgContent.startsWith("bad")){
                        guesserPanel.setHangmanImageStatus(8);
                    }
                    guesserPanel.disableAllButtons();
                    play = false;
                    break;
                }
                case UPDATE_PHRASE_STATUS: {
                    guesserPanel.setPhraseStatus(serverMsg.substring(4));
                    break;
                }
                case UPDATE_MISTAKES_STATUS: {
                    guesserPanel.setHangmanImageStatus(Integer.parseInt(serverMsg.substring(4)) + 1);
                    break;
                }
            }
        }
        guesserPanel.enableNextRoundButton();
    }

    private void observeRound(String startPhraseStatus, String[] players) {
        PanelFactory.ObserverGamePanel observerPanel = PanelFactory.getObserverGamePanel(startPhraseStatus, output, players);
        clientAppWindow.loadContentPane(observerPanel);

        boolean play = true;
        while(play) {
            String serverMsg;
            try {
                serverMsg = input.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            MessageProtocol serverMsgProtocol = MessageProtocol.getMessageProtocol(serverMsg.substring(0, 4));

            switch(serverMsgProtocol) {
                case ALL_FINISHED_ROUND: {
                    observerPanel.enableNextRoundButton();
                    play = false;
                    break;
                }
                case UPDATE_OBSERVER_STATUS: {
                    String[] msgContent = serverMsg.substring(4).split(",");
                    if(msgContent[1].startsWith("#")){
                        observerPanel.setHangmanImageStatus(
                                Integer.parseInt(msgContent[1].substring(1)) + 1, msgContent[0]);
                    } else {
                       observerPanel.setPhraseStatus(msgContent[1], msgContent[0]);
                    }
                    break;
                }
            }
        }
    }
}
