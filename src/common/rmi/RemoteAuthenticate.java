package common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/12/13
 * Time: 7:08 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RemoteAuthenticate extends Remote
{
	public boolean authenticate(String name, String pass) throws RemoteException;
}
