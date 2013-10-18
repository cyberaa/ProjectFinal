package common.rmi;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/15/13
 * Time: 4:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class NotEnoughSharesException extends Exception
{
	public NotEnoughSharesException()
	{
		super();
	}

	public NotEnoughSharesException(String message)
	{
		super (message);
	}

	public NotEnoughSharesException(Throwable cause)
	{
		super (cause);
	}

	public NotEnoughSharesException(String message, Throwable cause)
	{
		super (message, cause);
	}

	@Override
	public String toString()
	{
		return "Idea does not have enough shares to complete action.";
	}
}
