package common;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/12/13
 * Time: 6:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionInfo
{
	String seller;
	String buyer;

	int parts;
	int total;

	public TransactionInfo(String seller, String buyer, int parts, int total)
	{
		this.seller = seller;
		this.buyer = buyer;
		this.parts = parts;
		this.total = total;
	}

	public String toString()
	{
		return "Seller: "+seller+"\tBuyer: "+buyer+"\tParts: "+parts+"\tTotal: "+total;
	}
}
