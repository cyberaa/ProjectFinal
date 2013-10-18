package common.rmi;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/13/13
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserAuthenticationException extends Exception
{
	public UserAuthenticationException()
	{
        super();
	}

	public UserAuthenticationException(String message)
	{
		super (message);
	}

	public UserAuthenticationException(Throwable cause)
	{
		super (cause);
	}

	public UserAuthenticationException(String message, Throwable cause)
	{
		super (message, cause);
	}

	@Override
	public String toString()
	{
		return "Invalid user or password.";
	}
}
