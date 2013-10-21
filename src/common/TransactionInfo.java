package common;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/12/13
 * Time: 6:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionInfo implements Serializable
{
	private static final long serialVersionUID = -6346265992255318206L;

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
