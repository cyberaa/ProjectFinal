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
    public Notifications(Socket sock)
    {
        shutdown = false;
        this.sock = sock;
        try {
            inStream = new ObjectInputStream(sock.getInputStream());
            System.out.println(inStream);
            System.out.println("Notification inStream created.");
        } catch (Exception ioe) {
            System.out.println("Error establishing notification socket.\n"+ ioe );
        }
        start();
    }


    @Override
    public void run() {
        String notification = "";
        System.out.println("Merda");
        gui = new ClientGUI();
        while (!shutdown) {
            try {
                System.out.println("Getting notification.");
                notification = (String) inStream.readObject();
                System.out.println("Notification received.");
                gui.notifyUser(notification);
            } catch (IOException ioe) {
                System.out.println(ioe);
	            Client.reconnect = true;
                shutdown = true;
            } catch (ClassNotFoundException clfe) {
                System.out.println(clfe);
                shutdown = true;
            }
        }

        gui.dispose();

        try {
            inStream.close();
            sock.close();
        } catch (IOException ioe) {
           System.out.println(ioe);
        }
    }
}
