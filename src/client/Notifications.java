package client;

import com.sun.corba.se.impl.io.IIOPInputStream;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/21/13
 * Time: 5:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class Notifications extends Thread {

    public Socket sock;
    public ObjectInputStream inStream;
    public boolean shutdown;
    public ClientGUI gui;

    /**
     * Create notification thread in client side to receive notifications from server.
     * @param sock
     */
    public Notifications(Socket sock) {
        shutdown = false;
        this.sock = sock;
        try {
            inStream = new ObjectInputStream(sock.getInputStream());
            System.out.println("Notification inStream created.");
        } catch (IOException ioe) {
            System.out.println("Error establishing notification socket.\n"+ ioe );
        }
        gui = new ClientGUI();
        start();
    }


    @Override
    public void run() {
        String notification = "";
        int max = 3;
        int tries = 0;

        while (!shutdown && tries<max) {
            try {
                notification = (String) inStream.readObject();
                System.out.println("Getting notification.");
                gui.notifyUser(notification);
            } catch (EOFException eofe) {
                System.out.println("EOF Notifications: "+eofe);
                try {
                    Thread.sleep(9000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                if(tries<max) {
                   if (reconnectToServer()) {
                       tries=0;
                   }
                   else {
                       tries++;
                       try {
                           Thread.sleep(5000);
                       } catch (InterruptedException e) {
                           e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                       }
                   }
                }
                else {
                    shutdown = true;
                }
            } catch (IOException ioe) {
                System.out.println(ioe);
                shutdown = true;
            } catch (ClassNotFoundException clfe) {
                System.out.println(clfe);
                shutdown = true;
            }
        }

        try {
            inStream.close();
            sock.close();
        } catch (IOException ioe) {
           System.out.println(ioe);
        }
    }

    public boolean reconnectToServer() {
        try {
            sock.close();
            System.out.println("\nReconnecting Notifications\n");
            sock = new Socket(Client.serverAddress_1, Client.server1_not_port);
            System.out.println("Socket Established Notifications");
            inStream = new ObjectInputStream(sock.getInputStream());
            System.out.println("Reconnected Notifications.");
        } catch (IOException ioe) {
            return false;
        }
        return true;
    }
}
