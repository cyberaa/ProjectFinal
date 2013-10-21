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
            System.out.println("Usage: java ServerTCP <server_connection_port> <server_notify_port> <rmi_registry_port> <rmi_registry_address>");
            return;
        }

	    //Get command line arguments.
        conPort = Integer.parseInt(args[0]);
	    notPort = Integer.parseInt(args[1]);
        rmiServerAddress = args[3];
        rmiRegistryPort = Integer.parseInt(args[2]);

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

        Socket s;

        while (true)
        {
            try {
                s = conListenSocket.accept();
                new UserConnection(s);
            } catch (SocketTimeoutException e) {
	            //Do nothing.
            } catch (IOException e) {
	            System.out.print("UserConnection listen socket error:\n" + e);
	            return;
            }
	        try {
		        s = notListenSocket.accept();
		        //new Notifications(s); //TODO: implement thread to deal with notifications.
	        } catch (SocketTimeoutException e) {
		        //Do nothing.
	        } catch (IOException e) {
		        System.out.print("UserConnection listen socket error:\n" + e);
		        return;
	        }

	        //TODO: UDP fail-over stuff.
        }
    }
}
