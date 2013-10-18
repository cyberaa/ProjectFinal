package common.rmi;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/13/13
 * Time: 11:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExistingUserException extends Exception
{
	public ExistingUserException()
	{
       super();
	}

	public ExistingUserException(String message)
	{
		super (message);
	}

	public ExistingUserException(Throwable cause)
	{
		super (cause);
	}

	public ExistingUserException(String message, Throwable cause)
	{
		super (message, cause);
	}

	@Override
	public String toString()
	{
		return "Username already in use.";
	}
}
