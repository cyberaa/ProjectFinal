package common.rmi;

import common.NotificationInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/22/13
 * Time: 10:31 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RemoteNotifications extends Remote
{
	public void insertNotification(int user_id, String not) throws RemoteException, SQLException;

	public ArrayList<NotificationInfo> getNotifications(int user_id) throws RemoteException, SQLException;

	public void removeNotifications(ArrayList<NotificationInfo> not_ids) throws RemoteException, SQLException;
}
