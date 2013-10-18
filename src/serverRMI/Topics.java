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
     * Insert a new Topic in database if not exists.
     * @param name New topic name.
     * @throws RemoteException
     * @throws ExistingTopicException
     */
    public void newTopic(String name) throws RemoteException, ExistingTopicException, SQLException {

        Connection db = ServerRMI.pool.connectionCheck();

        int tries = 0;
        int maxTries = 3;
        PreparedStatement stmt = null;
        ResultSet rs;

        getTopicID(name);

        String query = "INSERT INTO topic (id, text) VALUES (1,?)";

        try {
            db.setAutoCommit(false);
            stmt = db.prepareStatement(query);
            stmt.setString(1,name);
            rs = stmt.executeQuery();
            db.commit();
        } catch (SQLException e) {
            if(db != null) {
                db.rollback();
            }
            throw new SQLException();
        } finally {
            if(stmt != null) {
                stmt.close();
            }
            db.setAutoCommit(true);
        }
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
        PreparedStatement stmt;
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
                db = ServerRMI.pool.connectionCheck();
                if(tries++ > maxTries) {
                    throw new SQLException();
                }
            }
        }
        return topics;
    }



    public int getTopicID(String text) throws RemoteException, SQLException, ExistingTopicException {

        Connection db = ServerRMI.pool.connectionCheck();

        int tries = 0;
        int maxTries = 3;
        PreparedStatement stmt = null;
        ResultSet rs;

        String query = "SELECT topic.id FROM topic WHERE topic.text LIKE ?";

        while(tries < maxTries)
        {
            try {
                stmt = db.prepareStatement(query);
                stmt.setString(1, text);
                rs = stmt.executeQuery();
                if(rs.next()) {
                    throw new ExistingTopicException();
                }
                return rs.getInt("id");
            } catch (SQLException e) {
                db = ServerRMI.pool.connectionCheck();
                if(tries++ > maxTries) {
                    throw new SQLException();
                }
            }
        }
        return -1;
    }
}
