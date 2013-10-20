package client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/19/13
 * Time: 9:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class Client {

    protected static Socket s;
    protected static String serverAddress_1;
    protected static String serverAddress_2;
    protected static int serverPort1;
    protected static int serverPort2;

    public static void main(String args[]) {

        if(args.length != 4)
        {
            System.out.println("Usage: java ServerTCP <server1_address> <server1_port> <server2_address> <server2_port>");
            return;
        }

        serverAddress_1 = args[0];
        serverPort1 = Integer.parseInt(args[1]);

        serverAddress_2 = args[2];
        serverPort2 = Integer.parseInt(args[3]);

        try {
            s = new Socket(serverAddress_1, serverPort1);
        } catch(IOException ioe) {
            System.out.println("Error in socket creation.\n" + ioe);
        }

        String username;
        String password;

        Scanner sc = new Scanner(System.in);
        System.out.print("Username: ");
        username = sc.next();
        System.out.print("Password: ");
        password = sc.next();

    }

}
