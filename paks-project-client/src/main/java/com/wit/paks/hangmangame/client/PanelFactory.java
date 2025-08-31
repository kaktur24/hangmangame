package com.wit.paks.hangmangame.client;

import com.wit.paks.hangmangame.MessageProtocol;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PanelFactory {
    public static JPanel getWaitingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("",
                "[grow][c][grow]", "[grow][][grow]"));

        JLabel label = new JLabel("Wait for other players");
        label.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));

        panel.add(label, "cell 1 1");
        return panel;
    }

    public static JPanel getUsernameInputPanel(ClientAppWindow clientAppWindow) {
        JLabel label = new JLabel("Enter your username:");
        JTextField textField = new JTextField(15);
        JButton button = new JButton("Play");

        button.addActionListener(e -> {
            textField.setEditable(false);
            String userName = textField.getText();
            if(StringUtils.isAlphanumeric(userName)) {
                try {
                    new GameController(clientAppWindow, userName).start();
                    button.setEnabled(false);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Server connection error has been occurred",
                            "Connection error", JOptionPane.ERROR_MESSAGE);
                    textField.setEditable(true);
                }
                return;
            }
            JOptionPane.showMessageDialog(null,
                    "Username can only consist of alphanumeric characters",
                    "Incorrect username", JOptionPane.WARNING_MESSAGE);
            textField.setText("");
            textField.setEditable(true);
        });

        return getStandardInputPanel(label, textField, button);
    }

    public static JPanel getPhraseInputPanel(PrintWriter output) {
        JLabel label = new JLabel("Enter your phrase:");
        JTextField textField = new JTextField(20);
        JButton button = new JButton("Ready");

        button.addActionListener(e -> {
            textField.setEditable(false);
            String userPhrase = textField.getText().toUpperCase();
            if(StringUtils.isAsciiPrintable(userPhrase) && StringUtils.isAlpha(userPhrase)){
                output.println("" + MessageProtocol.INPUT_PHRASE + userPhrase);
                button.setEnabled(false);
                return;
            }
            JOptionPane.showMessageDialog(null,
                    "Phrase can only consist of letters",
                    "Incorrect phrase", JOptionPane.WARNING_MESSAGE);
            textField.setText("");
            textField.setEditable(true);
        });

        return getStandardInputPanel(label, textField, button);
    }

    public static ObserverGamePanel getObserverGamePanel(String phraseStatus, PrintWriter output, String[] players) {
        return new ObserverGamePanel(players[0], players[1], players[2], phraseStatus, output);
    }

    public static GuesserGamePanel getGuesserGamePanel(String phraseStatus, PrintWriter output) {
        return new GuesserGamePanel(phraseStatus, output);
    }

    public static JPanel getRankingPanel(String ranking, PrintWriter output) {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("",
                "[grow][200]60[200][grow]", "[grow][300]10[][grow]"));
        panel.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));

        String[] header = new String[]{"Player", "Points"};
        String[][] data = new String[5][2];
        data[0] = header;
        String[] dataStrTab = ranking.split("[,:]");
        for(int i=0; i < data.length - 1; i++){
            data[i + 1] = new String[]{dataStrTab[i*2], dataStrTab[i*2+1]};
        }
        JTable rankingTable = new JTable(data, header);
        rankingTable.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
        rankingTable.setRowHeight(50);
        rankingTable.setEnabled(false);

        DefaultTableCellRenderer centeringCellRenderer = new DefaultTableCellRenderer();
        centeringCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        rankingTable.setDefaultRenderer(Object.class, centeringCellRenderer);

        TableColumnModel tableColumnModel = rankingTable.getColumnModel();
        tableColumnModel.getColumn(0).setPreferredWidth(230);
        tableColumnModel.getColumn(1).setPreferredWidth(100);

        JButton nextGameButton = new JButton("Next Game");
        nextGameButton.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
        nextGameButton.setPreferredSize(new Dimension(150, 50));
        nextGameButton.addActionListener(e -> {
            output.println(MessageProtocol.PLAY_NEXT_GAME);
        });
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
        exitButton.setPreferredSize(new Dimension(150, 50));
        exitButton.addActionListener(e -> {
            output.println(MessageProtocol.END_CONNECTION);
        });

        panel.add(rankingTable, "cell 1 1 2 1, align center");
        panel.add(nextGameButton, "cell 1 2, align right");
        panel.add(exitButton, "cell 2 2, align left");
        return panel;
    }

    private static JPanel getStandardInputPanel(JLabel label, JTextField textField, JButton button) {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("",
                "[grow][c][grow]", "[grow][]10[]20[][grow]"));

        Font font = new Font(Font.DIALOG, Font.PLAIN, 22);
        label.setFont(font);
        textField.setFont(font);
        button.setFont(font);

        panel.add(label, "cell 1 1");
        panel.add(textField, "cell 1 2");
        panel.add(button, "cell 1 3");

        return panel;
    }

    public static class GuesserGamePanel extends JPanel {
        private final JLabel phraseLabel;
        private final JLabel hangmanImageLabel;
        private final JButton nextRoundButton;
        private final PrintWriter output;
        private final JButton[] letterButtons;

        private GuesserGamePanel(String phraseStatus, PrintWriter output) {
            super();
            setLayout(new MigLayout("",
                    "[grow][500!]10[380!][grow]", "[grow][700!][grow]"));

            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new MigLayout("",
                    "[380!,c]", "[500!,c][150!,c]"));
            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new MigLayout("",
                    "[90]10[90]10[90]10[90]10[90]",
                    "[150!,c]20[]10[]10[]10[]10[]10[][grow]"));

            phraseLabel = new JLabel(phraseStatus);
            phraseLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 28));

            hangmanImageLabel = new JLabel(getScaledImage("/hangman-1.png", 360));

            nextRoundButton = new JButton("Finish Round");
            nextRoundButton.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
            nextRoundButton.setEnabled(false);
            nextRoundButton.addActionListener(e -> {
                output.println(MessageProtocol.CONFIRM_FINISHING_ROUND);
                ((JButton)e.getSource()).setEnabled(false);
            });

            letterButtons = generateLettersButtons();
            this.output = output;

            leftPanel.add(phraseLabel, "cell 0 0 5 1, alignx center");
            int i = 0, j = 1;
            for(JButton btn : letterButtons){
                leftPanel.add(btn, "cell " + i + " " + j + ", width 60 , height 40, alignx center");
                i = (++i) % 5;
                if(i == 0) j++;
            }
            rightPanel.add(hangmanImageLabel, "cell 0 0");
            rightPanel.add(nextRoundButton, "cell 0 1");

            add(leftPanel, "cell 1 1");
            add(rightPanel, "cell 2 1");
        }

        public void setPhraseStatus(String phraseStatus) {
            phraseLabel.setText(phraseStatus);
        }

        public void setHangmanImageStatus(int imageNum) {
            hangmanImageLabel.setIcon(
                    getScaledImage("/hangman-" + imageNum + ".png", 360));
        }

        public void disableAllButtons() {
            for(JButton btn : this.letterButtons) {
                btn.setEnabled(false);
            }
        }

        public void enableNextRoundButton() {
            nextRoundButton.setEnabled(true);
        }

        private JButton[] generateLettersButtons() {
            JButton[] letterButtons = new JButton['Z' - 'A' + 1];
            Font font = new Font(Font.DIALOG, Font.PLAIN, 20);

            for(char c = 'A'; c <= 'Z'; c++){
                char letter = c;
                JButton button = new JButton("" + letter);
                button.setFont(font);
                button.addActionListener(e -> {
                    output.println("" + MessageProtocol.GUESS_LETTER + letter);
                    ((JButton)e.getSource()).setEnabled(false);
                });
                letterButtons[c - 'A'] = button;
            }
            return letterButtons;
        }
    }

    public static class ObserverGamePanel extends JPanel {
        private final Map<String, JPanel> playersPanels;
        private final JButton nextRoundButton;

        private ObserverGamePanel(String p1, String p2, String p3, String phrase, PrintWriter output) {
            super();
            setLayout(new GridLayout(2, 2));

            String[] players = new String[]{p1, p2, p3};
            this.playersPanels = new HashMap<>();
            for(String player : players){
                JPanel panel = new JPanel();
                panel.setLayout(new MigLayout("",
                        "[grow][c][grow]", "[grow][c]5[c]5[c][grow]"));

                JLabel playerName = new JLabel(player);
                playerName.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
                JLabel hangmanImage = new JLabel(getScaledImage("/hangman-1.png", 240));
                JLabel phraseStatus = new JLabel(phrase);
                phraseStatus.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));

                panel.add(playerName, "cell 1 1");
                panel.add(hangmanImage, "cell 1 2");
                panel.add(phraseStatus, "cell 1 3");

                playersPanels.put(player, panel);
                add(panel);
            }

            JPanel panel = new JPanel();
            panel.setLayout(new MigLayout("",
                    "[grow][c][grow]", "[grow][c][grow]"));
            JButton nextRoundButton = new JButton("Finish Round");
            nextRoundButton.setEnabled(false);
            nextRoundButton.setFont(new Font(Font.DIALOG, Font.PLAIN, 22));
            nextRoundButton.addActionListener(e -> {
                output.println(MessageProtocol.CONFIRM_FINISHING_ROUND);
                ((JButton)e.getSource()).setEnabled(false);
            });
            this.nextRoundButton = nextRoundButton;
            panel.add(nextRoundButton, "cell 1 1");
            add(panel);
        }

        public void setPhraseStatus(String phraseStatus, String playerName) {
            JPanel panel = playersPanels.get(playerName);
            ((JLabel)panel.getComponent(2)).setText(phraseStatus);
        }

        public void setHangmanImageStatus(int imageNum, String playerName) {
            JPanel panel = playersPanels.get(playerName);
            ((JLabel)panel.getComponent(1)).setIcon(
                    getScaledImage("/hangman-" + imageNum + ".png", 240));
        }

        public void enableNextRoundButton() {
            nextRoundButton.setEnabled(true);
        }
    }

    private static ImageIcon getScaledImage(String path, int size) {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(Objects.requireNonNull(PanelFactory.class.getResource(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ImageIcon(bufferedImage.getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }
}
