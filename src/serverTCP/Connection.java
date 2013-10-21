package serverTCP;

import common.rmi.*;
import common.tcp.*;

import java.io.EOFException;
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
	//Socket and streams.
	protected Socket clientSocket;
	protected ObjectInputStream inStream;
	protected ObjectOutputStream outStream;

	//Remote objects.
	protected RemoteUserManager um;
	protected RemoteIdeas ideas;
	protected RemoteTopics topics;
	protected RemoteTransactions transactions;

	protected boolean shutdown = false;
	protected int userID;

    public Connection(Socket cSocket)
    {
        clientSocket = cSocket;

        try {
	        outStream = new ObjectOutputStream(cSocket.getOutputStream());
            inStream = new ObjectInputStream(cSocket.getInputStream());
        } catch (IOException ie) {
	        System.out.println("Could not create input and output streams:\n" + ie);
        }

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
	    Object cmd;

	    authenticateOrRegister();

	    while(!shutdown)
	    {
		    //Read next command.
		    try {
			    cmd = inStream.readObject();
		    } catch (ClassNotFoundException cnfe) {
			    System.out.println("Object class not found:\n" + cnfe);
			    continue;
		    } catch (EOFException eofe) {
			    System.out.println("Client disconnected.");
			    return;
		    } catch (IOException ioe) {
			    System.out.println("Could not read from socket:\n" + ioe);
			    continue;
		    }

		    //TODO: create a new thread to deal with notifications to send to user.

		    //Interpret and execute command. Send answer back.
		    executeCommand(cmd);
	    }
    }

	/**
	 * Execute the next command.
	 * @param cmd The next command to be executed.
	 */
	protected void executeCommand(Object cmd)
	{
		if(cmd instanceof CreateTopic)
		{
			CreateTopic aux = (CreateTopic) cmd;
			try {
				topics.newTopic(aux.name);
				sendInt(0);
			} catch (ExistingTopicException e) {
				//Send information that topic already exists.
				sendInt(-2);
			} catch (Exception e) {
				//Send information that topic creation failed but not because it already exists.
				sendInt(-1);
			}
		}
		else if(cmd instanceof ListTopics)
		{
			try {
				sendObject(topics.listTopics());
			} catch (Exception e) {
				sendInt(-1);
			}
		}
		else if(cmd instanceof SubmitIdea)
		{
			SubmitIdea aux = (SubmitIdea) cmd;
			try {
				ideas.submitIdea(aux.topics, userID, aux.parent_id, aux.number_parts, aux.part_val, aux.stance, aux.text);
				sendInt(0);
			} catch (Exception e) {
				//Send information that idea was not correctly submitted.
				sendInt(-1);
			}
		}
		else if(cmd instanceof DeleteIdea)
		{
			DeleteIdea aux = (DeleteIdea) cmd;
			try {
				ideas.deleteIdea(aux.idea_id, userID);
				sendInt(0);
			} catch (NotFullOwnerException e) {
				//Send information that to delete idea one must own all of its shares.
				sendInt(-2);
			} catch (Exception e) {
				//Send information that deletion was unsuccessful.
				sendInt(-1);
			}
		}
		else if(cmd instanceof ViewIdeasTopic)
		{
			ViewIdeasTopic aux = (ViewIdeasTopic) cmd;
			try {
				sendObject(ideas.viewIdeasTopic(aux.topic_id));
			} catch (Exception e) {
				//Send information that requested data cannot be fetched.
				sendInt(-1);
			}
		}
		else if(cmd instanceof ViewIdeasNested)
		{
			ViewIdeasNested aux = (ViewIdeasNested) cmd;
			try {
				sendObject(ideas.viewIdeasNested(aux.idea_id));
			} catch (Exception e) {
				//Send information that requested data cannot be fetched.
				sendInt(-1);
			}
		}
		else if(cmd instanceof SetShareValue)
		{
			SetShareValue aux = (SetShareValue) cmd;
			try {
				transactions.setShareValue(userID, aux.idea_id, aux.new_value);
				sendInt(0);
			} catch (Exception e) {
				//Send information that requested data cannot be fetched.
				sendInt(-1);
			}
		}
		else if(cmd instanceof BuyShares)
		{
			BuyShares aux = (BuyShares) cmd;
			try {
				transactions.buyShares(aux.user_id, aux.idea_id, aux.share_num, aux.price_per_share, aux.new_price_share);
				sendInt(0);
			} catch (Exception e) {
				//Send information that requested data cannot be fetched.
				sendInt(-1);
			}
		}
		else if(cmd instanceof ViewIdeasShares)
		{
			ViewIdeasShares aux = (ViewIdeasShares) cmd;
			try {
				sendObject(transactions.getShares(aux.idea_id));
			} catch (Exception e) {
				//Send information that requested data cannot be fetched.
				sendInt(-1);
			}
		}
		else if(cmd instanceof ShowHistory)
		{
			try {
				sendObject(transactions.showHistory(userID));
			} catch (Exception e) {
				//Send information that requested data cannot be fetched.
				sendInt(-1);
			}
		}
	}

	/**
	 * Wait for user authentication but allowing for registration
	 * in the meantime.
	 */
	protected void authenticateOrRegister()
	{
		Object cmd = null;

		int ret = -1;
		boolean success = false;
		while(!success)
		{
			//Read next command.
			try {
				cmd = inStream.readObject();
			} catch (ClassNotFoundException cnfe) {
				System.out.println("Object class not found:\n" + cnfe);
				ret = -1;
			} catch (EOFException eofe) {
				System.out.println("Client disconnected.");
				shutdown = true;
				return;
			} catch (IOException ioe) {
				System.out.println("Could not read from socket:\n" + ioe);
				ret = -1;
			}

			//Interpret and execute command.
			if(cmd == null)
				continue;
			else if(cmd instanceof Register)
			{
				Register aux = (Register) cmd;
				try {
					um.register(aux.name, aux.pass, aux.nameAlias);
					ret = 0;
				} catch (ExistingUserException e) {
					ret = -2;
				} catch (Exception e) {
					ret = -1;
				}
			}
			else if(cmd instanceof Authenticate)
			{
				Authenticate aux = (Authenticate) cmd;
				try {
					userID = um.authenticate(aux.username, aux.password);
					success = true;
					ret = 0;
				} catch (UserAuthenticationException e) {
					ret = -2;
				} catch (Exception e) {
					ret = -1;
				}
			}

			//Send return.
			sendInt(ret);
		}
	}

	/**
	 * Send an <em>Integer</em> to the client.
	 * @param value The integer to be sent.
	 */
	protected void sendInt(int value)
	{
		try {
			outStream.writeObject(new Integer(value));
			outStream.flush();
		} catch (EOFException e) {
			System.out.println("Client disconnected.");
			shutdown = true;
			return;
		} catch (IOException ioe) {
			System.out.println("Could not read from socket:\n" + ioe);
		}
	}

	/**
	 * Send an object. If the object cannot be sent it is put
	 * in that user's notification queue.
	 * @param obj
	 */
	//TODO: allow for passing of a notification.
	protected void sendObject(Object obj)
	{
		try {
			outStream.writeObject(obj);
			outStream.flush();
		} catch (EOFException e) {
			System.out.println("Client disconnected.");
			shutdown = false;
		} catch (IOException e) {
			//TODO: put notification in a queue.
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
