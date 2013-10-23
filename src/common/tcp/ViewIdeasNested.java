package common.tcp;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/20/13
 * Time: 4:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewIdeasNested implements Serializable {

	private static final long serialVersionUID = -1630261562598510737L;

	public int idea_id;
    public boolean loadAttach;

    public ViewIdeasNested(int idea_id, boolean loadAttach) {
        this.idea_id = idea_id;
        this.loadAttach = loadAttach;
    }
}
