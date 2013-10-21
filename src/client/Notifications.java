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

    public Notifications(Socket sock) {
        shutdown = false;
        this.sock = sock;
        try {
            inStream = new ObjectInputStream(sock.getInputStream());
            System.out.println("In created");
        } catch (IOException ioe) {
            System.out.println("Error establishing notification socket.\n"+ ioe );
        }
        start();
    }


    @Override
    public void run() {

    }
}
