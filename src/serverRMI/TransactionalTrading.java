package serverRMI;

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
	public synchronized static void enqueue(int user_id, int idea_id, int share_num, int price_per_share, int new_price_share)
	{
		PreparedStatement enqueue = null;
		String query = "INSERT INTO transaction_queue VALUES (transaction_queue_id_inc.nextval, systimestamp, ?, ?, ?, ?, ?)";

		try {
			Connection db = ServerRMI.pool.connectionCheck();
			boolean success = false;
			while(!success)
			{
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
					System.out.println(e);
					success = false;
				} finally {
					if(enqueue != null)
						enqueue.close();
				}
			}

			ServerRMI.pool.releaseConnection(db);
		} catch (SQLException e) {
			//Gosto imenso de vaginas.
		}
	}

	/**
	 *
	 * @param idea_id
	 */
	//FIXME: implement database commits and rollbacks correctly.
	public synchronized static void checkQueue(int idea_id) throws SQLException
	{
		Connection db = ServerRMI.pool.connectionCheck();
		PreparedStatement getQueue = null;

		try {
			String query = "SELECT * FROM transaction_queue WHERE idea_id = ? ORDER BY timestamp ASC";
			ResultSet rs = null;

			//Get relevant queue.
			boolean success = false;
			while(!success)
			{
				try {
					getQueue = db.prepareStatement(query);
					getQueue.setInt(1, idea_id);

					rs = getQueue.executeQuery();

					success = true;
				} catch (SQLException e) {
					success = false;
				} finally {
					if(!success && getQueue != null)
						getQueue.close();
				}
			}

			//Return if result set was not correctly fetched.
			if(rs == null)
				return;

			//Retry transaction on everything in the queue.
			while(rs.next())
			{
				try {
					int res = ServerRMI.transactions.buyShares(rs.getInt("user_id"), rs.getInt("idea_id"), rs.getInt("share_num"), rs.getInt("price_per_share"), rs.getInt("new_price_share"), true);

					if(res == -1)
					{
						removeFromQueue(db, rs.getInt("id"));
					}
				} catch (Exception e) {
					System.out.println(e);
					continue;
				}
			}

			db.commit();
		} catch (SQLException e) {
			System.out.println("\n"+e+"\n");
			if(db != null)
				db.rollback();
		} finally {
			if(getQueue != null)
			{
				try {
					getQueue.close();
				} catch (SQLException e) {
					//Cannot close PreparedStatement, ignore.
				}
			}
		}

		ServerRMI.pool.releaseConnection(db);
	}

	/**
	 *
	 * @param db
	 * @param id
	 */
	protected synchronized static void removeFromQueue(Connection db, int id)
	{
		PreparedStatement dequeue = null;
		String query = "DELETE FROM transaction_queue WHERE id = ?";

		boolean success = false;
		while(!success)
		{
			try {

				try {
					dequeue = db.prepareStatement(query);
					dequeue.setInt(1, id);

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
