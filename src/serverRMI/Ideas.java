package serverRMI;

import common.IdeaInfo;
import common.IdeasNestedPack;
import common.rmi.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
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
    public void submitIdea(ArrayList<String> topics, int user_id, int parent_id, int number_parts, int part_val, int stance, String text, byte[] fileData, String filename, int current) throws RemoteException, SQLException, IOException
    {
        if (!filename.equals("-")) {
            FileOutputStream fos = new FileOutputStream("assets/"+filename);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            bos.write(fileData, 0 , current);
            bos.flush();
            bos.close();
        }

        Connection db = ServerRMI.pool.connectionCheck();

        int tries = 0;
        int maxTries = 3;
        PreparedStatement stmt = null;
        ArrayList<Integer> topicIds = new ArrayList<Integer>();
        ResultSet rs;

	    try {
		    db.setAutoCommit(false);

		    String query = "SELECT * FROM idea WHERE user_id = ? AND parent_id = ? AND number_parts = ? AND stance = ? AND active = ? AND text LIKE ? AND filename = ?";

		    try {
			    stmt = db.prepareStatement(query);
			    stmt.setInt(1, user_id);
			    stmt.setInt(2, parent_id);
			    stmt.setInt(3, number_parts);
			    stmt.setInt(4, stance);
			    stmt.setInt(5, 1);
			    stmt.setString(6, text);
			    stmt.setString(7, filename);

			    rs = stmt.executeQuery();

			    if(rs.next())
				    return;
		    } catch (SQLException e) {
			    System.out.println(e);
			    if(db != null) {
				    db.rollback();
			    }
		    } finally {
			    if(stmt != null) {
				    stmt.close();
			    }
		    }

		    //Get topic ID.
		    String aux;
            int topic_id;
            System.out.println("Size 2: "+topics.size());
		    for(int i=0; i < topics.size(); i++)
		    {
			    aux = topics.get(i);
			    try {
                    topic_id = ServerRMI.topics.getTopicID(aux);
                    if (topic_id != -1) {
                        topicIds.add(topic_id);
                    }
                    else {
                        topic_id = ServerRMI.topics.newTopic(aux);
                        topicIds.add(topic_id);
                    }
			    } catch (ExistingTopicException ete) {
                    ServerRMI.pool.releaseConnection(db);
				    // Topic already exists
			    }
		    }

		    //Insert idea.
		    query = "INSERT INTO idea (id,user_id,parent_id,number_parts,stance,active,text,attach) VALUES (idea_id_inc.nextval,?,?,?,?,?,?,?)";

            try {
                stmt = db.prepareStatement(query);
                stmt.setInt(1, user_id);
                stmt.setInt(2, parent_id);
                stmt.setInt(3, number_parts);
                stmt.setInt(4, stance);
                stmt.setInt(5, 1);
                stmt.setString(6, text);
                stmt.setString(7, filename);

                stmt.executeQuery();
            } catch (SQLException e) {
                System.out.println(e);
                if(db != null) {
                    db.rollback();
                }
            } finally {
                if(stmt != null) {
                    stmt.close();
                }
            }

            int idea_id = 0;

            query = "SELECT idea_id_inc.currval as id FROM dual";

            try {
                stmt = db.prepareStatement(query);

                rs = stmt.executeQuery();

                rs.next();

                idea_id = rs.getInt("id");
            } catch (SQLException e) {
                System.out.println(e);
                if(db != null) {
                    db.rollback();
                }
            } finally {
                if(stmt != null) {
                    stmt.close();
                }
            }


		    //Create relationship between topic and idea.
		    query = "INSERT INTO idea_has_topic (idea_id,topic_id) VALUES (?,?)";

		    for(int i=0; i < topicIds.size(); i++)
		    {

                try {
                    stmt = db.prepareStatement(query);
                    stmt.setInt(1, idea_id);
                    stmt.setInt(2, topicIds.get(i));

                    stmt.executeQuery();
                } catch (SQLException e) {
                    if(db != null) {
                        db.rollback();
                    }
                } finally {
                    if(stmt != null) {
                        stmt.close();
                    }
                }

		    }

            //Insert initial share of idea
            query = "INSERT INTO shares (id,idea_id,user_id,parts,value) VALUES (shares_id_inc.nextval,?,?,?,?)";

            try {
                stmt = db.prepareStatement(query);
                stmt.setInt(1,idea_id);
                stmt.setInt(2,user_id);
                stmt.setInt(3,number_parts);
                stmt.setInt(4,part_val);

                stmt.executeQuery();
            } catch (SQLException e) {
                System.out.println(e);
                if(db != null) {
                    db.rollback();
                }
            } finally {
                if(stmt != null) {
                    stmt.close();
                }
            }

		    db.commit();
	    } catch (SQLException e) {
		    System.out.println("\n"+e+"\n");
		    if(db != null)
			    db.rollback();
		    throw e;
	    } finally {
		    db.setAutoCommit(true);
		    ServerRMI.pool.releaseConnection(db);
	    }
	}

	/**
	 * Delete idea identified by <em>idea_id</em>.
	 * @param idea_id The identifier of the idea to delete.
	 * @param user_id The identifier of the user trying to delete the idea.
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws NotFullOwnerException
	 */
    public void deleteIdea(int idea_id, int user_id) throws RemoteException, SQLException, NotFullOwnerException
    {
	    Connection db = ServerRMI.pool.connectionCheck();

	    try {
		    //Verify that user owns all shares.
		    int numParts = ServerRMI.transactions.getNumberShares(db, idea_id);

		    int tries = 0;
		    int maxTries = 3;
		    PreparedStatement stmt = null;
		    ResultSet rs;

		    String verify = "SELECT parts FROM shares WHERE idea_id = ? AND user_id = ?";

		    while(tries < maxTries)
		    {
			    try {
				    stmt = db.prepareStatement(verify);
				    stmt.setInt(1, idea_id);
				    stmt.setInt(2, user_id);

				    rs = stmt.executeQuery();

				    if(rs.next())
				    {
					    if(rs.getInt("parts") == numParts)
						    break;
				    }
				    else
					    throw new NotFullOwnerException();
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

		    //Update active field to 0.
		    String update = "UPDATE idea SET active = 0 WHERE id = ?";

		    while(tries < maxTries)
		    {
			    try {
				    stmt = db.prepareStatement(update);
				    stmt.setInt(1, idea_id);

				    stmt.executeQuery();
				    break;
			    } catch (SQLException e) {
				    System.out.println(e);
				    if(tries++ > maxTries)
					    throw e;
			    } finally {
				    if(stmt != null)
					    stmt.close();
			    }
		    }

		    //Remove from idea_has_topic.
		    String remove = "DELETE FROM idea_has_topic WHERE idea_id = ?";
		    while(tries < maxTries)
		    {
			    try {
				    stmt = db.prepareStatement(remove);
				    stmt.setInt(1, idea_id);

				    stmt.executeQuery();
				    break;
			    } catch (SQLException e) {
				    System.out.println(e);
				    if(tries++ > maxTries)
					    throw e;
			    } finally {
				    if(stmt != null)
					    stmt.close();
			    }
		    }

		    //Remove from shares.
		    remove = "DELETE FROM shares WHERE idea_id = ?";
		    while(tries < maxTries)
		    {
			    try {
				    stmt = db.prepareStatement(remove);
				    stmt.setInt(1, idea_id);

				    stmt.executeQuery();
				    break;
			    } catch (SQLException e) {
				    System.out.println(e);
				    if(tries++ > maxTries)
					    throw e;
			    } finally {
				    if(stmt != null)
					    stmt.close();
			    }
		    }

		    db.commit();
	    } catch (SQLException e) {
		    System.out.println(e);
		    if(db != null)
			    db.rollback();
	    } finally {
		    db.setAutoCommit(true);
	    }
	}

	/**
	 *
	 * @param topic_id
	 * @return
	 * @throws RemoteException
	 * @throws SQLException
	 */
    public ArrayList<IdeaInfo> viewIdeasTopic(int topic_id) throws RemoteException, SQLException
    {
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
                    throw e;
                }
            } finally {
	            if(stmt != null)
		            stmt.close();
            }
        }
        System.out.println(ideas.size());
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
    public IdeasNestedPack viewIdeasNested(int idea_id, boolean loadAttach) throws RemoteException, SQLException, NonExistingIdeaException, IOException {

        Connection db = ServerRMI.pool.connectionCheck();

        int tries = 0;
        int maxTries = 3;
        PreparedStatement stmt = null;
        ResultSet rs;
        String query;
        ArrayList<IdeaInfo> ideas = new ArrayList<IdeaInfo>();
        byte[] fileData = null;
        int fileLength = 0;

        if (loadAttach) {
            try {
                query = "SELECT idea.attach as attach FROM idea WHERE idea.id = ?";
                stmt = db.prepareStatement(query);
                stmt.setInt(1, idea_id);

                rs = stmt.executeQuery();

                rs.next();

                String attachPath = rs.getString("attach");

                File fileToSend = new File("assets/"+attachPath);
                fileLength = (int)fileToSend.length();
                fileData = new byte[fileLength];
                FileInputStream fis = new FileInputStream(fileToSend);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(fileData,0,fileData.length);
            } catch (SQLException e) {
                System.out.println(e);
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }


        query = "SELECT idea.id, sduser.namealias, stance, text FROM idea, sduser WHERE parent_id = ? AND idea.user_id = sduser.id AND idea.active = 1";

        while(tries < maxTries)
        {
            try {
                stmt = db.prepareStatement(query);
                stmt.setInt(1, idea_id);

                rs = stmt.executeQuery();

                if(!rs.next()) {
                    break;
                }

                do {
                    ideas.add(new IdeaInfo(rs.getInt("id"), rs.getString("namealias"), rs.getString("text"), rs.getInt("stance")));
                } while(rs.next());

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

        IdeasNestedPack pack = new IdeasNestedPack(ideas, fileData, fileLength);

        return pack;
    }
}
