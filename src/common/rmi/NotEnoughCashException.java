package common.rmi;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/14/13
 * Time: 11:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class NotEnoughCashException extends Exception
{
	public NotEnoughCashException()
	{
		super();
	}

	public NotEnoughCashException(String message)
	{
		super (message);
	}

	public NotEnoughCashException(Throwable cause)
	{
		super (cause);
	}

	public NotEnoughCashException(String message, Throwable cause)
	{
		super (message, cause);
	}

	@Override
	public String toString()
	{
		return "User does not have enough cash to complete action.";
	}
}
