package common.rmi;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/20/13
 * Time: 4:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class NotFullOwnerException extends Exception
{
	public NotFullOwnerException()
	{
		super();
	}

	public NotFullOwnerException(String message)
	{
		super (message);
	}

	public NotFullOwnerException(Throwable cause)
	{
		super (cause);
	}

	public NotFullOwnerException(String message, Throwable cause)
	{
		super (message, cause);
	}

	@Override
	public String toString()
	{
		return "User does not own all idea shares.";
	}
}
