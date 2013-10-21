package common.tcp;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/20/13
 * Time: 4:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class SetShareValue implements Serializable {

	private static final long serialVersionUID = 6208565993764399537L;


	public int idea_id;
    public int new_value;

    public SetShareValue(int idea_id, int new_value)
    {
	    this.idea_id = idea_id;
        this.new_value = new_value;
    }
}
