package common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/12/13
 * Time: 7:08 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RemoteUserManager extends Remote
{
	public int authenticate(String name, String pass) throws RemoteException, UserAuthenticationException, SQLException;

	public void register(String name, String pass, String nameAlias) throws RemoteException, ExistingUserException, SQLException;
}
