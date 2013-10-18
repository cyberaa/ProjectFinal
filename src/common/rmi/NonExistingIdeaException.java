package common.rmi;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/15/13
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class NonExistingIdeaException extends Exception {

    public NonExistingIdeaException() {
        super();
    }

    public NonExistingIdeaException(String message)
    {
        super (message);
    }

    public NonExistingIdeaException(Throwable cause)
    {
        super (cause);
    }

    public NonExistingIdeaException(String message, Throwable cause)
    {
        super (message, cause);
    }

    @Override
    public String toString()
    {
        return "Idea does not exists.";
    }
}
