package serverRMI;

import common.IdeaInfo;
import common.rmi.ExistingTopicException;
import common.rmi.NonExistingIdeaException;
import common.rmi.RemoteIdeas;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/13/13
 * Time: 6:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class Ideas extends UnicastRemoteObject implements RemoteIdeas
{
    public Ideas() throws RemoteException{
        super();
    }

    /**
     * Insert new idea in database.
     * @param topics Topic text. If topic not exists, a new topic is created.
     * @param user_id User who posted idea.
     * @param parent_id
     * @param number_parts
     * @param part_val
     * @param stance
     * @param text
     * @throws RemoteException
     * @throws SQLException
     */


    public void submitIdea(ArrayList<String> topics, int user_id, int parent_id, int number_parts, int part_val, int stance, String text) throws RemoteException, SQLException {

        Connection db = ServerRMI.pool.connectionCheck();

        int tries = 0;
        int maxTries = 3;
        PreparedStatement stmt = null;
        ArrayList<Integer> topicIds = new ArrayList<Integer>();

	    try {
		    db.setAutoCommit(false);

		    //Get topic ID.
		    String aux;
		    for(int i=0; i < topics.size(); i++)
		    {
			    aux = topics.get(i);
			    try {
				    topicIds.add(ServerRMI.topics.getTopicID(aux));
				    ServerRMI.topics.newTopic(aux);
			    } catch (ExistingTopicException ete) {
				    // Topic already exists
			    }
		    }

		    //Insert idea.
		    String query = "INSERT INTO idea (id,user_id,parent_id,number_parts,part_val,stance,text) VALUES (idea_id_inc.nextval,?,?,?,?,?,?)";

		    while(tries < maxTries)
		    {
			    try {
				    stmt = db.prepareStatement(query);
				    stmt.setInt(1, user_id);
				    stmt.setInt(2, parent_id);
				    stmt.setInt(3, number_parts);
				    stmt.setInt(4, part_val);
				    stmt.setInt(5, stance);
				    stmt.setString(6, text);

				    stmt.executeQuery();

				    break;
			    } catch (SQLException e) {
				    if(db != null) {
					    db.rollback();
				    }
				    if(tries++ > maxTries) {
					    throw new SQLException();
				    }
				    db = ServerRMI.pool.connectionCheck();
			    }
		    }

		    //Create relationship between topic and idea.
		    query = "INSERT INTO idea_has_topic (idea_id,topic_id) VALUES (?,?)";

		    for(int i=0; i < topicIds.size(); i++)
		    {
			    while(tries < maxTries)
			    {
				    try {
					    stmt = db.prepareStatement(query);
					    stmt.setInt(1, 1);
					    stmt.setInt(1, topicIds.get(i));

					    stmt.executeQuery();

					    break;
				    } catch (SQLException e) {
					    if(db != null) {
						    db.rollback();
					    }
					    if(tries++ > maxTries) {
						    throw new SQLException();
					    }
				    } finally {
					    if(stmt != null) {
						    stmt.close();
					    }
				    }
			    }
		    }

		    db.commit();
	    } catch (SQLException e) {
		    System.out.println("\n"+e+"\n");
		    if(db != null)
			    db.rollback();
		    throw new SQLException();
	    } finally {
		    db.setAutoCommit(true);
	    }
	}

	/**
	 *
	 * @param idea_id
	 * @throws RemoteException
	 * @throws SQLException
	 */
    public void deleteIdea(int idea_id, int user_id) throws RemoteException, SQLException {

        //TODO: verify that user has 100% shares
	    //TODO: update active field to 0
    }

	/**
	 *
	 * @param topic_id
	 * @return
	 * @throws RemoteException
	 * @throws SQLException
	 */
    public ArrayList<IdeaInfo> viewIdeasTopic(int topic_id) throws RemoteException, SQLException {

        Connection db = ServerRMI.pool.connectionCheck();

        int tries = 0;
        int maxTries = 3;
        PreparedStatement stmt = null;
        ResultSet rs;
        ArrayList<IdeaInfo> ideas = new ArrayList<IdeaInfo>();

        String query = "SELECT idea.id, sduser.namealias, stance, text FROM idea, idea_has_topic, sduser WHERE topic_id = ? AND idea_id = idea.id AND idea.user_id = sduser.id AND idea.parent_id = 0 AND idea.active = 1";

        while(tries < maxTries)
        {
            try {
                stmt = db.prepareStatement(query);
                stmt.setInt(1, topic_id);

                rs = stmt.executeQuery();

                while(rs.next()) {
                    ideas.add(new IdeaInfo(rs.getInt("id"), rs.getString("namealias"), rs.getString("text"), rs.getInt("stance")));

                }
                break;
            } catch (SQLException e) {
                if(tries++ > maxTries) {
                    throw new SQLException();
                }
            } finally {
	            if(stmt != null)
		            stmt.close();
            }
        }

        return ideas;
    }

	/**
	 *
	 * @param idea_id
	 * @return
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws NonExistingIdeaException
	 */
    public ArrayList<IdeaInfo> viewIdeasNested(int idea_id) throws RemoteException, SQLException, NonExistingIdeaException {

        Connection db = ServerRMI.pool.connectionCheck();

        int tries = 0;
        int maxTries = 3;
        PreparedStatement stmt = null;
        ResultSet rs;
        ArrayList<IdeaInfo> ideas = new ArrayList<IdeaInfo>();

        String query = "SELECT idea.id, sduser.namealias, stance, text FROM idea, sduser WHERE parent_id = ? AND idea.user_id = sduser.id AND idea.active = 1";

        while(tries < maxTries)
        {
            try {
                stmt = db.prepareStatement(query);
                stmt.setInt(1, idea_id);

                rs = stmt.executeQuery();

                if(!rs.next()) {
                    throw new NonExistingIdeaException();
                }

                do {
                    ideas.add(new IdeaInfo(rs.getInt("id"), rs.getString("namealias"), rs.getString("text"), rs.getInt("stance")));
                } while(rs.next());

                break;
            } catch (SQLException e) {
                if(tries++ > maxTries) {
                    throw new SQLException();
                }
            } finally {
	            if(stmt != null)
		            stmt.close();
            }
        }

        return ideas;
    }

	/**
	 * Changes the value per share of the idea identified by <em>idea_id</em> to new_value.
	 * @param idea_id The identifier of the idea to update.
	 * @param new_value The new share value to have for the idea identified by <em>idea_id</em>.
	 * @throws RemoteException
	 * @throws SQLException
	 */
	public void setShareValue(int idea_id, int new_value) throws RemoteException, SQLException
	{
        Connection db = ServerRMI.pool.connectionCheck();

        PreparedStatement updateValue = null;

		String query = "UPDATE idea SET part_val = ? WHERE id = ?";

		try {
			db.setAutoCommit(false);

			updateValue = db.prepareStatement(query);
			updateValue.setInt(1, new_value);
			updateValue.setInt(2, idea_id);

			updateValue.executeQuery();

			db.commit();
		} catch (SQLException e) {
			System.out.println("\n"+e+"\n");
			if(db != null)
				db.rollback();
			throw new SQLException();
		}
		finally {
			if(updateValue != null)
				updateValue.close();

			db.setAutoCommit(true);
		}
	}
}
