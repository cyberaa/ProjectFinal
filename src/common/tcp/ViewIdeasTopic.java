package common.tcp;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/20/13
 * Time: 4:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewIdeasTopic implements Serializable {

	private static final long serialVersionUID = 8506156349359930961L;

	public int topic_id;

    public ViewIdeasTopic(int topic_id) {
        this.topic_id = topic_id;
    }
}
