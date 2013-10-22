package common.rmi;

import common.TopicInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/12/13
 * Time: 6:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RemoteTopics extends Remote
{
	public int newTopic(String name) throws RemoteException, ExistingTopicException, SQLException;

	public ArrayList<TopicInfo> listTopics() throws RemoteException, SQLException;
}
