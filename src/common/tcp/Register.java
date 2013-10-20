package common.tcp;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/20/13
 * Time: 4:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Register {

    public String name;
    public String pass;
    public String nameAlias;

    public Register(String name, String pass, String nameAlias) {
        this.name = name;
        this.pass = pass;
        this.nameAlias = nameAlias;
    }
}
