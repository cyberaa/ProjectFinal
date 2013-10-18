package common.rmi;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/15/13
 * Time: 9:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class NotEnoughSharesAtDesiredPriceException extends Exception
{
	public NotEnoughSharesAtDesiredPriceException()
	{
		super();
	}

	public NotEnoughSharesAtDesiredPriceException(String message)
	{
		super (message);
	}

	public NotEnoughSharesAtDesiredPriceException(Throwable cause)
	{
		super (cause);
	}

	public NotEnoughSharesAtDesiredPriceException(String message, Throwable cause)
	{
		super (message, cause);
	}

	@Override
	public String toString()
	{
		return "Not enough shares can be bought at desired price.";
	}
}
