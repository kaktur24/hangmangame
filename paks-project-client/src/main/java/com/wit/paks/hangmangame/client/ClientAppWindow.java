package com.wit.paks.hangmangame.client;

import javax.swing.*;
import java.awt.*;

public class ClientAppWindow extends JFrame {
    private JPanel contentPane;
    private JLabel userNameLabel;

    public ClientAppWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setResizable(false);
        setLocationRelativeTo(null);
        setTitle("Hangman game");

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        super.setContentPane(mainPanel);

        JPanel userNamePanel = new JPanel();
        userNamePanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel userNameLabel = new JLabel();
        userNameLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));

        this.userNameLabel = userNameLabel;
        userNamePanel.add(userNameLabel);
        mainPanel.add(userNamePanel, BorderLayout.NORTH);

        contentPane = PanelFactory.getUsernameInputPanel(this);
        setContentPane(contentPane);
        setVisible(true);
    }

    private void setContentPane(JPanel pane) {
        getContentPane().add(pane, BorderLayout.CENTER);
    }

    public void setUserName(String userName) {
        this.userNameLabel.setText(userName);
    }

    public  void loadContentPane(JPanel pane) {
        getContentPane().remove(contentPane);
        this.contentPane = pane;
        setContentPane(contentPane);
        getContentPane().validate();
    }
}
