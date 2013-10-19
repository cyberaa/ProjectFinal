package common.rmi;

import common.IdeaInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/12/13
 * Time: 6:33 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RemoteIdeas extends Remote
{
	public void submitIdea(ArrayList<String> topics, int user_id, int parent_id, int number_parts, int part_val, int stance, String text) throws RemoteException, SQLException;

	public void deleteIdea(int idea_id) throws RemoteException, SQLException;

	public ArrayList<IdeaInfo> viewIdeasTopic(int topic_id) throws RemoteException, SQLException;

	public ArrayList<IdeaInfo> viewIdeasNested(int idea_id) throws RemoteException, SQLException, NonExistingIdeaException;

	public void setShareValue(int share_id, int new_value) throws RemoteException, SQLException;
}
