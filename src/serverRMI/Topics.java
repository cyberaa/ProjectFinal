package serverRMI;

import common.TopicInfo;
import common.rmi.ExistingTopicException;
import common.rmi.RemoteTopics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/13/13
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Topics extends UnicastRemoteObject implements RemoteTopics {

    public Topics() throws RemoteException {

    }

    /**
     * Insert a new Topic in database if it does not exists.
     * @param name New topic name.
     * @throws RemoteException
     * @throws ExistingTopicException
     */
    public int newTopic(String name) throws RemoteException, ExistingTopicException, SQLException {

        Connection db = ServerRMI.pool.connectionCheck();

        PreparedStatement stmt = null;
        String query;

        int topic_id;

        ResultSet rs;

        getTopicID(name);

        try {

            db.setAutoCommit(false);

            query = "SELECT * FROM topic WHERE text LIKE ?";

            stmt = db.prepareStatement(query);
	        stmt.setString(1, name);

            rs = stmt.executeQuery();

            if(rs.next()) {
                throw new ExistingTopicException();
            }
	        if(stmt != null)
		        stmt.close();

            query = "INSERT INTO topic (id, text) VALUES (topic_id_inc.nextval,?)";

            stmt = db.prepareStatement(query);
            stmt.setString(1,name);

            stmt.executeQuery();

            query = "SELECT topic_id_inc.currval as id FROM dual";

	        if(stmt != null)
		        stmt.close();
            stmt = db.prepareStatement(query);

            rs = stmt.executeQuery();

            rs.next();

            topic_id = rs.getInt("id");

            db.commit();
        } catch (SQLException e) {
            System.out.println("Bode: "+e);
            if(db != null) {
                db.rollback();
            }
            throw e;
        } finally {
            if(stmt != null) {
                stmt.close();
            }
            db.setAutoCommit(true);
        }

	    ServerRMI.pool.releaseConnection(db);

        return topic_id;
    }

    /**
     * Get all topics in database.
     * @return List of all topics.
     * @throws RemoteException
     * @throws SQLException
     */
    public ArrayList<TopicInfo> listTopics() throws RemoteException, SQLException {

        Connection db = ServerRMI.pool.connectionCheck();

        ArrayList<TopicInfo> topics = new ArrayList<TopicInfo>();
        int tries = 0;
        int maxTries = 3;
        int id;
        String text;
        PreparedStatement stmt = null;
        ResultSet rs;
        String query = "SELECT * FROM topic";

        while(tries < maxTries)
        {
            try {
                stmt = db.prepareStatement(query);
                rs = stmt.executeQuery();

                while(rs.next()) {
                    id = rs.getInt("id");
                    text = rs.getString("text");
                    topics.add(new TopicInfo(id,text));
                }
                break;
            } catch (SQLException e) {
                System.out.println(e);
                if(tries++ > maxTries) {
                    throw e;
                }
            } finally {
	            if(stmt != null)
		            stmt.close();
            }
	    }

	    ServerRMI.pool.releaseConnection(db);

        return topics;
    }

	/**
	 *
	 * @param text
	 * @return
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ExistingTopicException
	 */
    public int getTopicID(String text) throws RemoteException, SQLException, ExistingTopicException {

        Connection db = ServerRMI.pool.connectionCheck();

        int tries = 0;
        int maxTries = 3;
        PreparedStatement stmt = null;
        ResultSet rs;

        String query = "SELECT topic.id as id FROM topic WHERE topic.text LIKE ?";

        while(tries < maxTries)
        {
            try {
                stmt = db.prepareStatement(query);
                stmt.setString(1, text);

                rs = stmt.executeQuery();

                if(!rs.next()) {
                    return -1;
                }
                return rs.getInt("id");
            } catch (SQLException e) {
                if(tries++ > maxTries) {
                    throw e;
                }
            } finally {
	            if(stmt != null)
		            stmt.close();
            }
        }

	    ServerRMI.pool.releaseConnection(db);

        return -1;
    }
}
