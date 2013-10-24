package client;

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

    public Notifications(Socket sock) {
        shutdown = false;
        this.sock = sock;
        try {
            inStream = new ObjectInputStream(sock.getInputStream());
        } catch (IOException ioe) {
            System.out.println("Error establishing notification socket.\n"+ ioe );
        }
        gui = new ClientGUI();
        start();
    }


    @Override
    public void run() {
        String notification = "";

        while (!shutdown) {
            try {
                notification = (String) inStream.readObject();
                gui.notifyUser(notification);
            } catch (IOException ioe) {
                System.out.println(ioe);
                shutdown = true;
            } catch (ClassNotFoundException clfe) {
                System.out.println(clfe);
                shutdown = true;
            }
        }
    }
}
