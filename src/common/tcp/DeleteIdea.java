package common.tcp;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/20/13
 * Time: 4:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeleteIdea implements Serializable {

	private static final long serialVersionUID = -3945309271203987397L;

	public int idea_id;
	public int user_id;

    public DeleteIdea(int idea_id, int user_id) {
        this.idea_id = idea_id;
	    this.user_id = user_id;
    }
}
