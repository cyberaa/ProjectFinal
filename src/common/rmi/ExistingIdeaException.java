package common.rmi;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/15/13
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExistingIdeaException extends Exception {

    public ExistingIdeaException() {
        super();
    }

    public ExistingIdeaException(String message)
    {
        super (message);
    }

    public ExistingIdeaException(Throwable cause)
    {
        super (cause);
    }

    public ExistingIdeaException(String message, Throwable cause)
    {
        super (message, cause);
    }

    @Override
    public String toString()
    {
        return "Topic already created.";
    }
}
