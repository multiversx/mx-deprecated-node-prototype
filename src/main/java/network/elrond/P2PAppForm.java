package network.elrond;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class P2PAppForm {
    private JButton startButton;
    private JPanel configurationPanel;
    private JTabbedPane tabPanelConfiguration;
    private JPanel confManagementPanel;
    private JPanel operationPanel;
    private JScrollPane statsPanel;
    private JPanel loggerPanel;
    private JTextField textFieldIp;
    private JLabel ipLabel;
    private JTextField textFieldYourPort;
    private JTextField textFieldIpConnect;
    private JTextField textFieldPortToConnect;
    private JCheckBox isFirstNode;
    private JTextField textFieldInstanceName;
    private JTextField textFieldPrivateKey;
    private JTextField textFieldPublicKey;
    private JTextArea textAreaOutput;

    public P2PAppForm() {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textAreaOutput.setText("Start Elrond protocol");
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        JFrame frame = new JFrame("P2PAppForm");

        JTabbedPane tabbedPane = new JTabbedPane();
        frame.setContentPane(new P2PAppForm().configurationPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setVisible(true);
    }
}
