package network.elrond;

import javax.swing.*;
import java.awt.*;
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
    private JToolBar logger;
    private JScrollPane loggerScrollPane;
    private final static String newline = "\n";

    public P2PAppForm() {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String yourIp = textFieldIp.getText();
                String yourPort = textFieldYourPort.getText();
                String ipToConnect = textFieldIpConnect.getText();
                String portToConnect = textFieldPortToConnect.getText();
                String instanceName = textFieldInstanceName.getText();
                String privateKey = textFieldPrivateKey.getText();

                textAreaOutput.append(yourIp + newline);
                textAreaOutput.append(yourPort + newline);
                textAreaOutput.append(ipToConnect + newline);
                textAreaOutput.append(portToConnect + newline);
                textAreaOutput.append(instanceName + newline);
                textAreaOutput.append(privateKey + newline);
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        JFrame frame = new JFrame("P2PAppForm");
        frame.setContentPane(new P2PAppForm().configurationPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setVisible(true);
    }
}
