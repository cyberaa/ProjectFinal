package common.tcp;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/20/13
 * Time: 4:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateTopic implements Serializable {

	private static final long serialVersionUID = -6185094221610527798L;

	public String name;

    public CreateTopic(String name) {
        this.name = name;
    }
}
