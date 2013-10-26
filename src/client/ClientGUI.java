package client;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/24/13
 * Time: 12:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class ClientGUI extends JFrame
{
    JLabel labelNot;


    public ClientGUI() {
        this.setLocationRelativeTo(null);
        labelNot = new JLabel();
        this.getContentPane().add(labelNot, BorderLayout.CENTER);
        this.setSize(500,100);
        setVisible(true);
    }

    public void notifyUser(String notif) {
        labelNot.setText(notif);
    }
}
