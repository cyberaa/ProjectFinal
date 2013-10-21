package common;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/13/13
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TopicInfo implements Serializable
{
	private static final long serialVersionUID = -1611944208123009834L;

	public int id;
	public String text;

	public TopicInfo(int id, String text)
	{
		this.id = id;
		this.text = text;
	}

	public String toString()
	{
		return "id = "+id+"\ttext = "+text;
	}
}
