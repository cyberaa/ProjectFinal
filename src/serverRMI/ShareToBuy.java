package serverRMI;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/15/13
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShareToBuy
{
	public int id;
	public int user_id;
	public int numToBuy;
	public int total;
	public int value;

	public ShareToBuy(int id, int user_id, int numToBuy, int total, int value)
	{
		this.id = id;
		this.user_id = user_id;
		this.numToBuy = numToBuy;
		this.total = total;
		this.value = value;
	}

	public String toString()
	{
		return "ID: "+id+"\tUser ID: "+user_id+"\tNum to buy: "+numToBuy+"\tTotal: "+total+"\tValue: "+value;
	}
}
