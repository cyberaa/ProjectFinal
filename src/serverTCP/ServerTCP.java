package serverTCP;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/15/13
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerTCP {

    //RMI UserConnection
    protected static String rmiServerAddress;
    protected static int rmiRegistryPort;

    //TCP UserConnection
    protected static int conPort;
    protected static ServerSocket conListenSocket;
	protected static int notPort;
	protected static ServerSocket notListenSocket;

	private static final int timeout = 10;

    public static void main(String args[]) {
        //Verify the number of given arguments.
        if(args.length != 4)
        {
            System.out.println("Usage: java ServerTCP <server_connection_port> <server_notify_port> <rmi_registry_address> <rmi_registry_port>");
            return;
        }

	    //Get command line arguments.
        conPort = Integer.parseInt(args[0]);
	    notPort = Integer.parseInt(args[1]);
        rmiServerAddress = args[2];
        rmiRegistryPort = Integer.parseInt(args[3]);

	    //Set system policies.
	    //System.getProperties().put("java.security.policy", "policy.all");
	    //System.setSecurityManager(new RMISecurityManager());

        try {
            conListenSocket = new ServerSocket(conPort);
	        conListenSocket.setSoTimeout(timeout);

	        notListenSocket = new ServerSocket(notPort);
	        notListenSocket.setSoTimeout(timeout);
        } catch (IOException ie) {
            System.out.println("Error in server socket creation.\n"+ ie);
        }

	    System.out.println("RMI server at: "+rmiServerAddress+":"+rmiRegistryPort);
	    System.out.println("Ready to accept connections.\nConnection port:\t"+conPort+"\nNotifications port:\t"+notPort);

        Socket s;
	    UserNotifications notifs;

        while (true)
        {
	        //TODO: UDP fail-over stuff.

	        try {
		        s = notListenSocket.accept();
		        notifs = new UserNotifications(s);
	        } catch (SocketTimeoutException e) {
		        //Do nothing.
		        continue;
	        } catch (IOException e) {
		        System.out.print("UserConnection listen socket error:\n" + e);
		        continue;
	        }

	        try {
                s = conListenSocket.accept();
                new UserConnection(s, notifs);
            } catch (SocketTimeoutException e) {
	            //Do nothing.
            } catch (IOException e) {
	            System.out.print("UserConnection listen socket error:\n" + e);
            }
        }
    }
}
