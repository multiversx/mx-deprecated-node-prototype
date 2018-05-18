package network.elrond;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class P2PAppForm {
    private JButton startButton;
    private JPanel configurationPanel;
    private JTextField yourIpField;
    private JTextField textField2;
    private JLabel setPortLabel;
    private JLabel ipConnectLabel;
    private JTextField ipToConnectField;
    private JLabel portToConnectLabel;
    private JTextField portToConnectField;
    private JCheckBox isFirstNodeCheckBox;
    private JLabel instanceNameLabel;
    private JTextField instanceNameField;
    private JTextField privateKeyField;
    private JLabel privateKeyLabel;
    private JLabel yourIpLabel;
    private JLabel publicKeyLabel;
    private JTextField publicKeyField;

    public P2PAppForm() {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Start Elrond proto1");
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("P2PAppForm");
        frame.setContentPane(new P2PAppForm().configurationPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setVisible(true);
    }
}
