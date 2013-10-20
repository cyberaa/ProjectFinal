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

    public int share_id;
    public int new_value;

    public SetShareValue(int share_id, int new_value) {
        this.share_id = share_id;
        this.new_value = new_value;
    }
}
