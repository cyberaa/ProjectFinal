package serverTCP;


import java.io.IOException;
import java.net.ServerSocket;
/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/15/13
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerTCP {

    //RMI Connection
    protected static String rmiServerAddress;
    protected static int rmiRegistryPort;

    //TCP Connection
    protected static int serverPort;
    protected static ServerSocket listenSocket;


    public static void main(String args[]) {
        //Verify the number of given arguments.
        if(args.length != 3)
        {
            System.out.println("Usage: java ServerTCP <server_port> <rmi_registry_port> <rmi_registry_address>");
            return;
        }

        serverPort = Integer.parseInt(args[0]);

        rmiServerAddress = args[2];

        rmiRegistryPort = Integer.parseInt(args[1]);

        try {
            listenSocket = new ServerSocket(serverPort);
        } catch (IOException ie) {
            System.out.println("Error in server socket creation.\n"+ ie);
        }

    }
}
