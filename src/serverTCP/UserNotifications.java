package serverTCP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/21/13
 * Time: 3:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserNotifications extends Thread
{
	//Socket and streams.
	protected Socket clientSocket;
	protected ObjectInputStream inStream;
	protected ObjectOutputStream outStream;

	protected boolean shutdown = false;
	protected int userID;

	public UserNotifications(Socket cSocket)
	{
		clientSocket = cSocket;

		try {
			outStream = new ObjectOutputStream(clientSocket.getOutputStream());
            System.out.println("Output created");
		} catch (IOException ie) {
			System.out.println("[Notifications] Could not create input and output streams:\n" + ie);
		}
	}

	@Override
	public void run()
	{
		System.out.println("Notifications thread started.");
	}

	/**
	 * Set userID.
	 * @param userID The value to set.
	 */
	public void setUserID(int userID)
	{
		this.userID = userID;
	}
}
