package common;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/14/13
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShareInfo implements Serializable
{
	private static final long serialVersionUID = 8008689959006952390L;

	public int id;
	public int idea_id;
	public int user_id;
	public int parts;
	public int value;

	public ShareInfo(int id, int idea_id, int user_id, int parts, int value)
	{
		this.id = id;
		this.idea_id = idea_id;
		this.user_id = user_id;
		this.parts = parts;
		this.value = value;
	}

	public String toString()
	{
		return "ID: "+id+"\tIdea ID: "+idea_id+"\tUser ID: "+user_id+"\tParts: "+parts+"\tValue: "+value;
	}
}
