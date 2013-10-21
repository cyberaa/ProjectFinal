package serverRMI;

import common.rmi.NotEnoughCashException;
import common.rmi.NotEnoughSharesException;

import java.rmi.RemoteException;
import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/21/13
 * Time: 11:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionalTrading
{
	/**
	 *
	 * @param user_id
	 * @param idea_id
	 * @param share_num
	 * @param price_per_share
	 * @param new_price_share
	 */
	public static void enqueue(int user_id, int idea_id, int share_num, int price_per_share, int new_price_share)
	{
		PreparedStatement enqueue = null;
		String query = "INSERT INTO transaction_queue VALUES (transaction_queue_id_inc.nextval, ?, ?, ?, ?, ?)";

		boolean success = false;
		while(!success)
		{
			try {
				Connection db = ServerRMI.pool.connectionCheck();

				try {
					enqueue = db.prepareStatement(query);
					enqueue.setInt(1, user_id);
					enqueue.setInt(2, idea_id);
					enqueue.setInt(3, share_num);
					enqueue.setInt(4, price_per_share);
					enqueue.setInt(5, new_price_share);

					enqueue.executeQuery();
					db.commit();

					success = true;
				} catch (SQLException e) {
					success = false;
				} finally {
					if(enqueue != null)
						enqueue.close();
				}
			} catch (SQLException e) {
				success = false;
			}
		}
	}

	/**
	 *
	 * @param idea_id
	 */
	public static void checkQueue(int idea_id)
	{
		PreparedStatement getQueue = null;
		String query = "SELECT * FROM transaction_queue WHERE idea_id = ? ORDER BY timestamp";
		ResultSet rs = null;

		//Get relevant queue.
		boolean success = false;
		while(!success)
		{
			try {
				Connection db = ServerRMI.pool.connectionCheck();

				try {
					getQueue = db.prepareStatement(query);
					getQueue.setInt(1, idea_id);

					rs = getQueue.executeQuery();

					success = true;
				} catch (SQLException e) {
					success = false;
				} finally {
					if(getQueue != null)
						getQueue.close();
				}
			} catch (SQLException e) {
				success = false;
			}
		}

		//Return if result set was not correctly fetched.
		if(rs == null)
			return;

		//Retry transaction on everything in the queue.
		try {
			while(rs.next())
			{
				try {
					int res = ServerRMI.transactions.buyShares(rs.getInt("user_id"), rs.getInt("idea_id"), rs.getInt("share_num"), rs.getInt("price_per_share"), rs.getInt("new_price_share"), true);

					if(res == -1)
					{
						removeFromQueue(rs.getTimestamp("timestamp"));
					}
				} catch (Exception e) {
					continue;
				}
			}
		} catch (SQLException e) {
		}
	}

	/**
	 *
	 * @param ts
	 */
	protected static void removeFromQueue(Timestamp ts)
	{
		PreparedStatement dequeue = null;
		String query = "DELETE FROM transaction_queue WHERE timestamp = ?";

		boolean success = false;
		while(!success)
		{
			try {
				Connection db = ServerRMI.pool.connectionCheck();

				try {
					dequeue = db.prepareStatement(query);
					dequeue.setTimestamp(1, ts);

					dequeue.executeQuery();
					db.commit();

					success = true;
				} catch (SQLException e) {
					success = false;
				} finally {
					if(dequeue != null)
						dequeue.close();
				}
			} catch (SQLException e) {
				success = false;
			}
		}
	}
}
