package common;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/23/13
 * Time: 9:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class NotificationInfo implements Serializable
{
	private static final long serialVersionUID = -6014680550399790615L;

	public int id;
	public int user_id;
	public String text;

	public NotificationInfo(int id, int user_id, String text)
	{
		this.id = id;
		this.user_id = user_id;
		this.text = text;
	}
}
