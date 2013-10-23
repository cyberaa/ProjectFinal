package serverRMI;

import common.NotificationInfo;
import common.rmi.RemoteNotifications;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/22/13
 * Time: 10:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class Notifications implements RemoteNotifications
{
	/**
	 * Insert a notification into the database. Notifications are
	 * fetched and delivered later.
	 * @param user_id The id of the user to whom the notification will be sent.
	 * @param not The notification itself.
	 * @throws RemoteException
	 * @throws SQLException
	 */
	public void insertNotification(int user_id, String not) throws RemoteException, SQLException
	{
		PreparedStatement insert = null;
		String query = "INSERT INTO notifications VALUES (notifications_id_inc.nextval, ?, ?)";

		Connection db = ServerRMI.pool.connectionCheck();

		try {
			insert = db.prepareStatement(query);
			insert.setInt(1, user_id);
			insert.setString(2, not);

			insert.executeQuery();
			db.commit();
		} catch (SQLException e) {
			if(db != null)
				db.rollback();
		} finally {
			if(insert != null)
				insert.close();
		}
	}

	/**
	 * Fetch all notifications which
	 * @param user_id
	 * @return
	 * @throws RemoteException
	 * @throws SQLException
	 */
	public ArrayList<NotificationInfo> getNotifications(int user_id) throws RemoteException, SQLException
	{
		ArrayList<NotificationInfo> ret = new ArrayList<NotificationInfo>();

		Connection db = ServerRMI.pool.connectionCheck();

		PreparedStatement getNotifications = null;
		String query = "SELECT * FROM notifications WHERE user_id = ?";
		ResultSet rs = null;

		try {
			getNotifications = db.prepareStatement(query);
			getNotifications.setInt(1, user_id);

			rs = getNotifications.executeQuery();

			while(rs.next())
				ret.add(new NotificationInfo(rs.getInt("id"), user_id, rs.getString("text")));

		} finally {
			if(getNotifications != null)
				getNotifications.close();
		}

		return ret;
	}

	/**
	 *
	 * @param not_ids
	 * @throws RemoteException
	 * @throws SQLException
	 */
	public void removeNotifications(ArrayList<NotificationInfo> not_ids) throws RemoteException, SQLException
	{
		PreparedStatement remove = null;
		String query = "DELETE FROM notifications WHERE id = ?";

		Connection db = ServerRMI.pool.connectionCheck();

		try {
			for(int i=0; i < not_ids.size(); i++)
			{
				remove = db.prepareStatement(query);
				remove.setInt(1, not_ids.get(i).id);

				remove.executeQuery();
			}

			db.commit();
		} catch (SQLException e) {
			if(db != null)
				db.rollback();
		} finally {
			if(remove != null)
				remove.close();
		}
	}
}
