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

    public int idea_id;

    public DeleteIdea(int idea_id) {
        this.idea_id = idea_id;
    }
}
