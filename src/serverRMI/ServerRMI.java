package serverRMI;

import common.rmi.ExistingTopicException;
import common.rmi.NotEnoughCashException;
import common.rmi.NotEnoughSharesException;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/12/13
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerRMI
{
	protected static final String dbUser = "sd2";
	protected static final String dbPass = "sd2";

	protected static int rmiPort;
	protected static Registry rmiRegistry;

	public static ConnectionPool pool;
	protected static String dbURL;

	protected static UserManager um;
	protected static Topics topics;
    protected static Ideas ideas;
	protected static Transactions transactions;
	protected static Notifications notifications;

	public static void main(String args[])
	{
		//Verify the number of given arguments.
		if(args.length != 2)
		{
			System.out.println("Usage: java ServerRMI <rmi_port> <db_IP_teste>");
			return;
		}

		//Set system policies.
		System.getProperties().put("java.security.policy", "policy.all");

		//Start RMI registry.
		rmiPort = Integer.parseInt(args[0]);
		startRMIRegistry();

		//Connect to database.
		dbURL = "jdbc:oracle:thin:@" + args[1] +":1521:XE";

        //Create pool of connections
        try {
            pool = new ConnectionPool(dbURL, dbUser, dbPass);
            System.out.println("Server connected to database.");
        } catch (SQLException se) {
            System.out.print("Error creating pool of connections.\n" + se);
            return;
        }

		//Create remote RMI objects and bind them.
		createAndBindObjects();

		//Menu.
        String command;
        Scanner sc = new Scanner(System.in);

        System.out.print("\nType \"help\" to see help menu.");

        while(true) {
            System.out.print("\n>> ");
            command = sc.next();
            if(command.equals("rmiport")) {
                System.out.println("\n RMI registry port: "+rmiPort);
            }
            else if (command.equals("dburl")) {
                System.out.println("\n Database URL: "+dbURL);
            }
            else if (command.equals("nconnections")) {
                System.out.println("\n Number of active connections to database: "+pool.connectionsUsed.size());
            }
            else if (command.equals("help")) {
                System.out.println("\n Commands: \n \t \t rmiport -> See RMI registry port. \n \t \t dburl -> See database url. \n \t \t nconnections -> Check number of active connections to database. \n \t \t exit -> Shutdown server.");
            }
            else if (command.equals("exit")) {
	            break;
            }
            else {
                System.out.println("\n"+command+": command not found");
            }
        }

        System.out.println("Starting RMI server shutdown...");

		//Unbind RMI objects and close their threads.
		unbindAndDestroyObjects();

		//Close RMI registry.
		try {
			UnicastRemoteObject.unexportObject(rmiRegistry, true);
			System.out.println("RMI registry successfully closed.");
		} catch (NoSuchObjectException e ) {
			System.out.println("Could not stop RMI registry:\n" + e);
		}
	}

	/**
	 * Start RMI registry.
	 */
	protected static void startRMIRegistry()
	{
		try {
			rmiRegistry = LocateRegistry.createRegistry(rmiPort);
		} catch (RemoteException e) {
			System.out.println("Could not start RMI registry:\n" + e);
		}

		System.out.println("RMI registry started successfully! ");
	}

	/**
	 * Create remote objects, save them in class variables and bind them.
	 */
	protected static void createAndBindObjects()
	{
		try {
			um = new UserManager();
			rmiRegistry.rebind("UserManager", um);

			topics = new Topics();
			rmiRegistry.rebind("Topics", topics);

            ideas = new Ideas();
            rmiRegistry.rebind("Ideas", ideas);

			transactions = new Transactions();
			rmiRegistry.rebind("Transactions", transactions);

			notifications = new Notifications();
			rmiRegistry.rebind("Notifications", notifications);

			System.out.println("Objects successfully bound to RMI registry.");
		} catch (RemoteException e) {
			System.out.println("Failed to create and bind RMI objects.\n" + e);
			System.exit(-1);
		}
	}

	/**
	 * Unbind remote objects and stop their RMI threads.
	 */
	protected static void unbindAndDestroyObjects()
	{
		try {
			rmiRegistry.unbind("UserManager");
			UnicastRemoteObject.unexportObject(um, true);

			rmiRegistry.unbind("Topics");
			UnicastRemoteObject.unexportObject(topics, true);

            rmiRegistry.unbind("Ideas");
            UnicastRemoteObject.unexportObject(ideas, true);

			rmiRegistry.unbind("Transactions");
			UnicastRemoteObject.unexportObject(transactions, true);

			rmiRegistry.unbind("Notifications");
			UnicastRemoteObject.unexportObject(notifications, true);

			System.out.println("Objects successfully unbound.");
		} catch (RemoteException re) {
			System.out.println("Could not unbind object:\n" + re);
			System.exit(-1);
		} catch (NotBoundException nbe) {
			//Do nothing, if it's already unbound we don't have to unbind it.
		}
	}
}
