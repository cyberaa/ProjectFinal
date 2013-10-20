package common.tcp;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/20/13
 * Time: 4:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowHistory implements Serializable {

    public int user_id;

    public ShowHistory(int user_id) {
        this.user_id = user_id;
    }

}