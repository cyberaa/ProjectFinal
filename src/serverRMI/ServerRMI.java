package serverRMI;

import common.IdeaInfo;
import common.TransactionInfo;
import common.rmi.*;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/12/13
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerRMI
{
	protected static final String dbUser = "sd2013";
	protected static final String dbPass = "sd2013";

	protected static int rmiPort;
	protected static Registry rmiRegistry;

	public static Connection db; //TODO: what if reconnectDB() is used while other thread is using the db?
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
			System.out.println("Usage: java ServerRMI <rmi_port> <db_IP>");
			return;
		}

		//Start RMI registry.
		rmiPort = Integer.parseInt(args[0]);
		startRMIRegistry();

		//Connect to database.
		dbURL = "jdbc:oracle:thin:@" + args[1] +":1521:XE";
		connectDB();
		if(db == null)
		{
			System.out.println("Could not connect to database!");
			System.exit(-1);
		}

		//Create remote RMI objects and bind them.
		createAndBindObjects();

		//TODO: More stuff.
		//TODO: test transactions.buyShares()

		try {
			ArrayList<TransactionInfo> aux = transactions.showHistory(3);
			for(int i=0; i < aux.size(); i++)
				System.out.println(aux.get(i));
        } catch (SQLException se) {
			System.out.println("Cannot register user:\n" + se);
		} catch (RemoteException re) {
			System.out.println("Cannot register user:\n" + re);
		}

		//Close connection to database.
		try {
			db.close();
			System.out.println("Connection to database closed successfully.");
		} catch (SQLException e) {
			System.out.println("Could not close connection to database:\n" + e);
		}

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

		System.out.println("RMI registry started successfully!");
	}

	/**
	 * Connect to database.
	 */
	protected static void connectDB()
	{
		try {
			db = DriverManager.getConnection(dbURL, dbUser, dbPass);
		} catch (SQLException e) {
			System.out.println("Connection to database failed:\n" + e);
			return;
		}

		System.out.println("Connection to database successful!");
	}

	/**
	 * Reconnect to database.
	 */
	public static void reconnectDB()
	{
		connectDB();
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
