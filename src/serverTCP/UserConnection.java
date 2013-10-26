package serverTCP;

import common.rmi.*;
import common.tcp.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/15/13
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserConnection extends Thread
{
	//Socket and streams.
	protected Socket clientSocket;
	protected ObjectInputStream inStream;
	protected ObjectOutputStream outStream;

	//Notifications thread.
	protected UserNotifications notifsThread;

	//Remote objects.
	protected RemoteUserManager um;
	protected RemoteIdeas ideas;
	protected RemoteTopics topics;
	protected RemoteTransactions transactions;

	protected boolean shutdown = false;
	protected int userID;

    public UserConnection(Socket cSocket, UserNotifications notifs)
    {
        int tries;
        int max = 3;
        int timeoutRMI = 5000;


        clientSocket = cSocket;
	    notifsThread = notifs;

        try {
	        inStream = new ObjectInputStream(cSocket.getInputStream());
	        outStream = new ObjectOutputStream(cSocket.getOutputStream());
        } catch (IOException ie) {
	        System.out.println("[Connection] Could not create input and output streams:\n" + ie);
        }

        Scanner sc = new Scanner(System.in);

        tries = 0;
        while (tries < max) {
            try {
                lookupRemotes();
                System.out.println("Objects lookup");
                break;
            } catch (RemoteException re) {
                System.out.println(re);
                if(tries < max) {
                    try {
                        System.out.println("Reconnecting to RMI.");
                        lookupRemotes();
                        System.out.println("Connected to RMI again.");
                    } catch (RemoteException e1) {
                        tries++;
                        try {
                            Thread.sleep(timeoutRMI);
                        } catch (InterruptedException e) {
                             System.out.println(e);
                        }
                    }
                }
                else {
                    System.exit(-1);
                }
            }
        }

	    start();
	}

    @Override
    public void run()
    {
	    Object cmd = null;

        try {
            authenticateOrRegister();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        //Start notifications thread.
	    notifsThread.setUserID(userID);
	    if(shutdown)
		    return;

	    notifsThread.start();

	    //Loop to receive commands.
	    while(!shutdown)
	    {
		    //Read next command.
		    try {
			    cmd = inStream.readObject();

		    } catch (ClassNotFoundException cnfe) {
			    System.out.println("Object class not found:\n" + cnfe);
			    continue;
		    } catch (EOFException eofe) {
			    shutdown = true;
		    } catch (IOException ioe) {
			    System.out.println("[1]Could not read from socket:\n" + ioe);
			    continue;
		    }

		    //Interpret and execute command. Send answer back.
            try {
                executeCommand(cmd);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

	    //Close streams and socket.
	    try {
		    inStream.close();
		    outStream.close();
		    clientSocket.close();
	    } catch (IOException e) {
		    //Do nothing, close thread.
	    }

	    notifsThread.shutdown = true;

	    System.out.println("Client disconnected.");
    }

	/**
	 * Execute the next command.
	 * @param cmd The next command to be executed.
	 */
	protected void executeCommand(Object cmd) throws InterruptedException {
        int max = 3;
        int tries;
        int timeoutRMI = 3000;



		if(cmd instanceof CreateTopic)
		{
            tries = 0;
            while(tries < max) {
                CreateTopic aux = (CreateTopic) cmd;
                try {
                    topics.newTopic(aux.name);
                    sendInt(0);
                    break;
                } catch (ExistingTopicException e) {
                    //Send information that topic already exists.
                    sendInt(-2);
                    break;
                } catch (RemoteException e) {
                    System.out.println(e);
                    if(tries < max) {
                        try {
                            System.out.println("Reconnecting to RMI.");
                            lookupRemotes();
                            System.out.println("Connected to RMI again.");
                        } catch (RemoteException e1) {
                            tries++;
                            Thread.sleep(timeoutRMI);
                        }
                    }
                    else {
                        //Send information that topic creation failed but not because it already exists.
                        sendInt(-3);
                        break;
                    }
                } catch (Exception e) {
                    sendInt(-1);
                    break;
                }
            }
		}
		else if(cmd instanceof ListTopics)
		{
            tries = 0;
            while(tries < max) {
                try {
                    sendObject(topics.listTopics());
                    break;
                } catch (RemoteException e) {
                    System.out.println(e);
                    if(tries < max) {
                        try {
                            System.out.println("Reconnecting to RMI.");
                            lookupRemotes();
                            System.out.println("Connected to RMI again.");
                        } catch (RemoteException e1) {
                            tries++;
                            Thread.sleep(timeoutRMI);
                        }
                    }
                    else {
                        //Send information that topic creation failed but not because it already exists.
                        sendInt(-3);
                        break;
                    }
                } catch (Exception e) {
                    sendInt(-1);
                    break;
                }
            }
		}
		else if(cmd instanceof SubmitIdea)
		{
			SubmitIdea aux = (SubmitIdea) cmd;
            tries = 0;
            while (tries < max) {
                try {
                    if (aux.fileAttachName.equals("-")) {
                        ideas.submitIdea(aux.topics, userID, aux.parent_id, aux.number_parts, aux.part_val, aux.stance, aux.text, null, "-", -1);
                    }
                    else {
                        Object fileLengthObj = inStream.readObject();
                        int fileLength = (Integer) fileLengthObj;
                        System.out.println("File Size: " + fileLength);
                        int bytesRead;
                        int current = 0;
                        byte[] bytesArray = new byte[5*1024*1024];
                        bytesRead = inStream.read(bytesArray,0,bytesArray.length);
                        System.out.print("File successfully read.");
                        current = bytesRead;
                        System.out.println("Read Complete: "+bytesRead+" Current: "+current);

                        do {
                            System.out.println("Reading");
                            bytesRead = inStream.read(bytesArray, current, (bytesArray.length - current));
                            if (bytesRead >= 0) {
                                current += bytesRead;
                            }
                            System.out.println("Read Complete: "+bytesRead+" Current: "+current);
                            if(current == fileLength)
                                break;
                        } while (bytesRead > -1);

                        ideas.submitIdea(aux.topics, userID, aux.parent_id, aux.number_parts, aux.part_val, aux.stance, aux.text, bytesArray, aux.fileAttachName, current);

                    }
                    sendInt(0);
                    break;
                } catch (RemoteException e) {
                    System.out.println(e);
                    if(tries < max) {
                        try {
                            System.out.println("Reconnecting to RMI.");
                            lookupRemotes();
                            System.out.println("Connected to RMI again.");
                        } catch (RemoteException e1) {
                            tries++;
                            Thread.sleep(timeoutRMI);
                        }
                    }
                    else {
                        //Send information that topic creation failed but not because it already exists.
                        sendInt(-3);
                        break;
                    }
                } catch (Exception e) {
                    sendInt(-1);
                    break;
                }
            }
		}
		else if(cmd instanceof DeleteIdea)
		{
			DeleteIdea aux = (DeleteIdea) cmd;
            tries = 0;
            while (tries < max) {
                try {
                    ideas.deleteIdea(aux.idea_id, userID);
                    sendInt(0);
                    break;
                } catch (NotFullOwnerException e) {
                    //Send information that to delete idea one must own all of its shares.
                    sendInt(-2);
                } catch (RemoteException e) {
                    System.out.println(e);
                    if(tries < max) {
                        try {
                            System.out.println("Reconnecting to RMI.");
                            lookupRemotes();
                            System.out.println("Connected to RMI again.");
                        } catch (RemoteException e1) {
                            tries++;
                            Thread.sleep(timeoutRMI);
                        }
                    }
                    else {
                        //Send information that topic creation failed but not because it already exists.
                        sendInt(-3);
                        break;
                    }
                } catch (Exception e) {
                    sendInt(-1);
                    break;
                }
            }
		}
		else if(cmd instanceof ViewIdeasTopic)
		{
            tries = 0;
			ViewIdeasTopic aux = (ViewIdeasTopic) cmd;
            while (tries < max) {
                try {
                    sendObject(ideas.viewIdeasTopic(aux.topic_id));
                    break;
                } catch (RemoteException e) {
                    System.out.println(e);
                    if(tries < max) {
                        try {
                            System.out.println("Reconnecting to RMI.");
                            lookupRemotes();
                            System.out.println("Connected to RMI again.");
                        } catch (RemoteException e1) {
                            tries++;
                            Thread.sleep(timeoutRMI);
                        }
                    }
                    else {
                        //Send information that topic creation failed but not because it already exists.
                        sendInt(-3);
                        break;
                    }
                } catch (Exception e) {
                    sendInt(-1);
                    break;
                }
            }
		}
		else if(cmd instanceof ViewIdeasNested)
		{
            tries = 0;
			ViewIdeasNested aux = (ViewIdeasNested) cmd;
            while (tries < max) {
                try {
                    if (aux.loadAttach)
                        sendObject(ideas.viewIdeasNested(aux.idea_id,true));
                    else
                        sendObject(ideas.viewIdeasNested(aux.idea_id,false));

                    break;
                } catch (RemoteException e) {
                    System.out.println(e);
                    if(tries < max) {
                        try {
                            System.out.println("Reconnecting to RMI.");
                            lookupRemotes();
                            System.out.println("Connected to RMI again.");
                        } catch (RemoteException e1) {
                            tries++;
                            Thread.sleep(timeoutRMI);
                        }
                    }
                    else {
                        //Send information that topic creation failed but not because it already exists.
                        sendInt(-3);
                        break;
                    }
                } catch (Exception e) {
                    sendInt(-1);
                    break;
                }
            }
		}
		else if(cmd instanceof SetShareValue)
		{
            tries = 0;
			SetShareValue aux = (SetShareValue) cmd;
            while (tries < max) {
                try {
                    transactions.setShareValue(userID, aux.idea_id, aux.new_value);
                    sendInt(0);
                    break;
                } catch (RemoteException e) {
                    System.out.println(e);
                    if(tries < max) {
                        try {
                            System.out.println("Reconnecting to RMI.");
                            lookupRemotes();
                            System.out.println("Connected to RMI again.");
                        } catch (RemoteException e1) {
                            tries++;
                            Thread.sleep(timeoutRMI);
                        }
                    }
                    else {
                        //Send information that topic creation failed but not because it already exists.
                        sendInt(-3);
                        break;
                    }
                } catch (Exception e) {
                    sendInt(-1);
                    break;
                }
            }
		}
		else if(cmd instanceof BuyShares)
		{
            tries = 0;
			BuyShares aux = (BuyShares) cmd;
            while (tries < max) {
                try {
                    transactions.buyShares(userID, aux.idea_id, aux.share_num, aux.price_per_share, aux.new_price_share, false);
                    sendInt(0);
                    break;
                } catch (RemoteException e) {
                    System.out.println(e);
                    if(tries < max) {
                        try {
                            System.out.println("Reconnecting to RMI.");
                            lookupRemotes();
                            System.out.println("Connected to RMI again.");
                        } catch (RemoteException e1) {
                            tries++;
                            Thread.sleep(timeoutRMI);
                        }
                    }
                    else {
                        //Send information that topic creation failed but not because it already exists.
                        sendInt(-3);
                        break;
                    }
                } catch (Exception e) {
                    sendInt(-1);
                    break;
                }
            }
		}
		else if(cmd instanceof ViewIdeasShares)
		{
            tries = 0;
            while (tries < max) {
                ViewIdeasShares aux = (ViewIdeasShares) cmd;
                try {
                    sendObject(transactions.getShares(aux.idea_id));
                    break;
                } catch (RemoteException e) {
                    System.out.println(e);
                    if(tries < max) {
                        try {
                            System.out.println("Reconnecting to RMI.");
                            lookupRemotes();
                            System.out.println("Connected to RMI again.");
                        } catch (RemoteException e1) {
                            tries++;
                            Thread.sleep(timeoutRMI);
                        }
                    }
                    else {
                        //Send information that topic creation failed but not because it already exists.
                        sendInt(-3);
                        break;
                    }
                } catch (Exception e) {
                    sendInt(-1);
                    break;
                }
            }
		}
		else if(cmd instanceof ShowHistory)
		{
            tries = 0;
            while (tries < max) {
                try {
                    sendObject(transactions.showHistory(userID));
                    break;
                } catch (RemoteException e) {
                    System.out.println(e);
                    if(tries < max) {
                        try {
                            System.out.println("Reconnecting to RMI.");
                            lookupRemotes();
                            System.out.println("Connected to RMI again.");
                        } catch (RemoteException e1) {
                            tries++;
                            Thread.sleep(timeoutRMI);
                        }
                    }
                    else {
                        //Send information that topic creation failed but not because it already exists.
                        sendInt(-3);
                        break;
                    }
                } catch (Exception e) {
                    sendInt(-1);
                    break;
                }
            }
		}
	}

	/**
	 * Wait for user authentication but allowing for registration
	 * in the meantime.
	 */
	protected void authenticateOrRegister() throws InterruptedException
	{

        int timeoutRMI = 3000;

		Object cmd = null;
        int max = 3;
        int tries;

		int ret = -1;
		boolean success = false;
		while(!success)
		{
			//Read next command.
			try {
				cmd = inStream.readObject();
				System.out.println(cmd);
			} catch (ClassNotFoundException cnfe) {
				System.out.println("Object class not found:\n" + cnfe);
				ret = -1;
			} catch (EOFException eofe) {
				System.out.println("Client disconnected.");
				shutdown = true;
				return;
			} catch (IOException ioe) {
				System.out.println("[2]Could not read from socket:\n" + ioe);
				ret = -1;
                break;
			}

			//Interpret and execute command.
			if(cmd == null)
				continue;
			else if(cmd instanceof Register)
			{
                tries = 0;
                while (tries < max)
                {
                    Register aux = (Register) cmd;
                    try {
                        um.register(aux.name, aux.pass, aux.nameAlias);
                        ret = 0;
                        break;
                    } catch (ExistingUserException e) {
                        ret = -2;
	                    break;
                    } catch (RemoteException e) {
                        System.out.println(e);
                        if(tries < max) {
                            try {
                                System.out.println("Reconnecting to RMI.");
                                lookupRemotes();
                                System.out.println("Connected to RMI again.");
                            } catch (RemoteException e1) {
                                tries++;
                                Thread.sleep(timeoutRMI);
                            }
                        }
                        else {
                            //Send information that topic creation failed but not because it already exists.
                            ret = -3;
                            break;
                        }
                    } catch (Exception e) {
                        ret = -1;
                        break;
                    }
                }
			}
			else if(cmd instanceof Authenticate)
			{
                tries = 0;
                while (tries < max) {
                    Authenticate aux = (Authenticate) cmd;
                    try {
                        userID = um.authenticate(aux.username, aux.password);
                        success = true;
                        ret = 0;
                        break;
                    } catch (UserAuthenticationException e) {
                        ret = -2;
	                    break;
                    } catch (RemoteException e) {
                        System.out.println(e);
                        if(tries < max) {
                            try {
                                System.out.println("Reconnecting to RMI.");
                                lookupRemotes();
                                System.out.println("Connected to RMI again.");
                            } catch (RemoteException e1) {
                                tries++;
                                Thread.sleep(timeoutRMI);
                            }
                        }
                        else {
                            ret = -3;
                            break;
                        }
                    } catch (Exception e) {
                        ret = -1;
                        break;
                    }
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
			System.out.println("[1]Could not write to socket:\n" + ioe);
		}
	}

	/**
	 * Send an object. If the object cannot be sent it is put
	 * in that user's notification queue.
	 * @param obj The object to be sent over the socket.
	 */
	protected void sendObject(Object obj)
	{
		try {
			outStream.writeObject(obj);
			outStream.flush();
		} catch (EOFException e) {
			System.out.println("Client disconnected.");
			shutdown = false;
		} catch (IOException e) {
			System.out.println("[2]Could not write to socket:\n" + e);
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
