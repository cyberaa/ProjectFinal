package serverTCP;

import common.rmi.*;
import common.tcp.Authenticate;
import common.tcp.CreateTopic;
import common.tcp.Register;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/15/13
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class Connection extends Thread
{
	protected Socket clientSocket;
	protected ObjectInputStream inStream;
	protected ObjectOutputStream outStream;

	//Remote objects.
	protected RemoteUserManager um;
	protected RemoteIdeas ideas;
	protected RemoteTopics topics;
	protected RemoteTransactions transactions;

    public Connection(Socket cSocket)
    {
        clientSocket = cSocket;

        /*try {
	        outStream = new ObjectOutputStream(cSocket.getOutputStream());
            inStream = new ObjectInputStream(cSocket.getInputStream());
        } catch (IOException ie) {
	        System.out.println("Could not create input and output streams:\n" + ie);
        }*/

	    try {
		    lookupRemotes();
	    } catch (RemoteException re) {
		    System.out.println("Error looking up remote objects:\n" + re);
	    }

	    start();
	}

    @Override
    public void run()
    {
	    try {
		    Authenticate auth = (Authenticate) inStream.readObject();
		    um.authenticate(auth.username, auth.password);
	    } catch (UserAuthenticationException e) {
		    //Send information back that authentication failed.
	    } catch (Exception e) {
		    //Send information that authentication failed but not due to given data.
	    }

	    Object cmd;

	    while(true)
	    {
		    try {
			    cmd = inStream.readObject();
		    } catch (ClassNotFoundException cnfe) {
			    System.out.println("Object class not found:\n" + cnfe);
			    continue;
		    } catch (IOException ioe) {
			    System.out.println("Object class not found:\n" + ioe);
			    continue;
		    }

		    if(cmd instanceof Register)
		    {
			    Register aux = (Register) cmd;
			    try {
				    um.register(aux.name, aux.pass, aux.nameAlias);
			    } catch (ExistingUserException e) {
				    //Send information that username is already in use.
			    } catch (Exception e) {
				    //Send information that registration failed but not because username is in use.
			    }
		    }
		    else if(cmd instanceof CreateTopic)
		    {
			    CreateTopic aux = (CreateTopic) cmd;
			    try {
				    topics.newTopic(aux.name);
			    } catch (ExistingTopicException e) {
				    //Send information that topic already exists.
			    } catch (Exception e) {
				    //Send information that topic creation failed but not because it already exists.
			    }
		    }
	    }
    }

	/**
	 * Lookup the remote objects and save them to class variables.
	 * @throws RemoteException
	 */
	protected void lookupRemotes() throws RemoteException
	{
		String rmiAddress = "rmi://"+ServerTCP.rmiServerAddress+":"+ServerTCP.rmiRegistryPort+"/";

		try {
			um = (RemoteUserManager) Naming.lookup(rmiAddress+"UserManager");
			ideas = (RemoteIdeas) Naming.lookup(rmiAddress+"Ideas");
			topics = (RemoteTopics) Naming.lookup(rmiAddress+"Topics");
			transactions = (RemoteTransactions) Naming.lookup(rmiAddress+"Transactions");
		} catch (MalformedURLException mue) {
			System.out.println("Wrong URL passed as argument:\n" + mue);
			System.exit(-1);
		} catch (NotBoundException nbe) {
			System.out.println("Object is not bound:\n" + nbe);
			System.exit(-1);
		}
	}
}
