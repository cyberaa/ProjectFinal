package common.rmi;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/13/13
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExistingTopicException extends Exception {

    public ExistingTopicException() {
        super();
    }

    public ExistingTopicException(String message)
    {
        super (message);
    }

    public ExistingTopicException(Throwable cause)
    {
        super (cause);
    }

    public ExistingTopicException(String message, Throwable cause)
    {
        super (message, cause);
    }

    @Override
    public String toString()
    {
        return "Topic already created.";
    }
}
