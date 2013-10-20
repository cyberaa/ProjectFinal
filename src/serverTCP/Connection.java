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
	protected Socket clientSocket;
	protected ObjectInputStream inStream;
	protected ObjectOutputStream outStream;

	//Remote objects.
	protected RemoteUserManager um;
	protected RemoteIdeas ideas;
	protected RemoteTopics topics;
	protected RemoteTransactions transactions;

	protected boolean shutdown = false;

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
	    //TODO: registration occurs before authentication.
	    authenticateUser();

	    Object cmd;

	    while(!shutdown)
	    {
		    try {
			    cmd = inStream.readObject();
		    } catch (ClassNotFoundException cnfe) {
			    System.out.println("Object class not found:\n" + cnfe);
			    continue;
		    } catch (EOFException eofe) {
			    System.out.println("Client disconnected.");
			    return;
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
		    else if(cmd instanceof ListTopics)
		    {
			    ListTopics aux = (ListTopics) cmd;
			    try {
				    outStream.writeObject(topics.listTopics());
			    } catch (EOFException e) {
				    System.out.println("Client disconnected.");
				    return;
			    } catch (IOException e) {
				    //Could not write to socket, what now?!
			    } catch (Exception e) {
				    //Send information that server was unable to fetch topics list.
			    }
		    }
		    else if(cmd instanceof SubmitIdea)
		    {
			    SubmitIdea aux = (SubmitIdea) cmd;
			    try {
				    ideas.submitIdea(aux.topics, aux.user_id, aux.parent_id, aux.number_parts, aux.part_val, aux.stance, aux.text);
			    } catch (Exception e) {
				    //Send information that idea was not correctly submitted.
			    }
		    }
		    else if(cmd instanceof DeleteIdea)
		    {
			    DeleteIdea aux = (DeleteIdea) cmd;
			    try {
				    ideas.deleteIdea(aux.idea_id, aux.user_id);
			    } catch (NotFullOwnerException e) {
				    //Send information that to delete idea one must own all of its shares.
			    } catch (Exception e) {
				    //Send information that deletion was unsuccessful.
			    }
		    }
		    else if(cmd instanceof ViewIdeasTopic)
		    {
			    ViewIdeasTopic aux = (ViewIdeasTopic) cmd;
			    try {
				    outStream.writeObject(ideas.viewIdeasTopic(aux.topic_id));
			    } catch (EOFException e) {
				    System.out.println("Client disconnected.");
				    return;
			    } catch (IOException e) {
				    //Could not write to socket, what now?!
			    } catch (Exception e) {
				    //Send information that requested data cannot be fetched.
			    }
		    }
		    else if(cmd instanceof ViewIdeasNested)
		    {
			    ViewIdeasNested aux = (ViewIdeasNested) cmd;
			    try {
				    outStream.writeObject(ideas.viewIdeasNested(aux.idea_id));
			    } catch (EOFException e) {
				    System.out.println("Client disconnected.");
				    return;
			    } catch (IOException e) {
				    //Could not write to socket, what now?!
			    } catch (Exception e) {
				    //Send information that requested data cannot be fetched.
			    }
		    }
		    else if(cmd instanceof SetShareValue)
		    {
			    SetShareValue aux = (SetShareValue) cmd;
			    try {
				    transactions.setShareValue(aux.user_id, aux.share_id, aux.new_value);
			    } catch (Exception e) {
				    //Send information that requested data cannot be fetched.
			    }
		    }
		    else if(cmd instanceof BuyShares)
		    {
			    BuyShares aux = (BuyShares) cmd;
			    try {
				    transactions.buyShares(aux.user_id, aux.idea_id, aux.share_num, aux.price_per_share, aux.new_price_share);
			    } catch (Exception e) {
				    //Send information that requested data cannot be fetched.
			    }
		    }
		    else if(cmd instanceof ViewIdeasShares)
		    {
			    ViewIdeasShares aux = (ViewIdeasShares) cmd;
			    try {
				    outStream.writeObject(transactions.getShares(aux.idea_id));
			    } catch (EOFException e) {
				    System.out.println("Client disconnected.");
				    return;
			    } catch (IOException e) {
				    //Could not write to socket, what now?!
			    } catch (Exception e) {
				    //Send information that requested data cannot be fetched.
			    }
		    }
		    else if(cmd instanceof ShowHistory)
		    {
			    ShowHistory aux = (ShowHistory) cmd;
			    try {
				    outStream.writeObject(transactions.showHistory(aux.user_id));
			    } catch (EOFException e) {
				    System.out.println("Client disconnected.");
				    return;
			    } catch (IOException e) {
				    //Could not write to socket, what now?!
			    } catch (Exception e) {
				    //Send information that requested data cannot be fetched.
			    }
		    }
	    }
    }

	/**
	 * Authenticate user after logging in.
	 */
	protected void authenticateUser()
	{
		boolean success = false;
		try {
			Authenticate auth = (Authenticate) inStream.readObject();
			um.authenticate(auth.username, auth.password);
			outStream.writeBoolean(true);
			success = true;
		} catch (UserAuthenticationException e) {
			//Send information back that authentication failed.
			return;
		} catch (Exception e) {
			//Send information that authentication failed but not due to given data.
			return;
		}
		if(!success)
		{
			try {
				outStream.writeBoolean(false);
			} catch (EOFException e) {
				System.out.println("Client disconnected.");
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
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
