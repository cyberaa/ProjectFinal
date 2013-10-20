package common.tcp;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/20/13
 * Time: 4:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewIdeasShares implements Serializable {

	private static final long serialVersionUID = 2764721960482085347L;

	public int idea_id;

    public ViewIdeasShares(int idea_id) {
        this.idea_id = idea_id;
    }
}
