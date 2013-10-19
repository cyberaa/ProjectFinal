package serverRMI;

import common.TransactionInfo;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
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

    //TODO: Peguntar ao paneleiro do Maxi como configurar o .gitignore para não fazer push do workspace


	protected static final String dbUser = "sd2013";
	protected static final String dbPass = "sd2013";

	protected static int rmiPort;
	protected static Registry rmiRegistry;

	public static ConnectionPool pool;
	protected static String dbURL;

	protected static UserManager um;
	protected static Topics topics;
    protected static Ideas ideas;
	protected static Transactions transactions;

	public static void main(String args[])
	{
		//Verify the number of given arguments.
		if(args.length != 2)
		{
			System.out.println("Usage: java ServerRMI <rmi_port> <db_IP_teste>");
			return;
		}

		//Start RMI registry.
		rmiPort = Integer.parseInt(args[0]);
		startRMIRegistry();

		//Connect to database.
		dbURL = "jdbc:oracle:thin:@" + args[1] +":1521:XE";

        //Create pool of connections
        try {
            pool = new ConnectionPool(dbURL, dbUser, dbPass);
        } catch (SQLException se) {
            System.out.print("Error creating pool of connections.\n" + se);
        }


		//Create remote RMI objects and bind them.
		createAndBindObjects();

		//TODO: More stuff.
		//TODO: test transactions.buyShares()

        String comand = "";

        Scanner sc = new Scanner(System.in);

        System.out.print("\nType \"help\" to see help menu.");

        while(!comand.equals("exit")) {
            System.out.print("\n>> ");
            comand = sc.next();
            if(comand.equals("rmiport")) {
                System.out.println("\n RMI registry port: "+rmiPort);
            }
            else if (comand.equals("dburl")) {
                System.out.println("\n Database URL"+dbURL);
            }
            else if (comand.equals("nconnections")) {
                System.out.println("\n Number of active connections to database: "+pool.connectionsUsed.size());
            }
            else if (comand.equals("help")) {
                System.out.println("\n Commands: \n \t \t rmiport -> See RMI registry port. \n \t \t dburl -> See database url. \n \t \t nconnections -> Check number of active connections to database.    ");
            }
            else {
                System.out.println("\n"+comand+": command not found");
            }
        }

        System.out.println("Starting RMI server shutdown...");

		/*try {
			ArrayList<TransactionInfo> aux = transactions.showHistory(3);
			for(int i=0; i < aux.size(); i++)
				System.out.println(aux.get(i));
        } catch (SQLException se) {
			System.out.println("Cannot register user:\n" + se);
		} catch (RemoteException re) {
			System.out.println("Cannot register user:\n" + re);
		}*/

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

			System.out.println("Objects successfully unbound.");
		} catch (RemoteException re) {
			System.out.println("Could not unbind object:\n" + re);
			System.exit(-1);
		} catch (NotBoundException nbe) {
			//Do nothing, if it's already unbound we don't have to unbind it.
		}
	}
}
