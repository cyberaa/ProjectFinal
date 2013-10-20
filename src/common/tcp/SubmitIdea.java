package common.tcp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/20/13
 * Time: 4:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubmitIdea implements Serializable {

	private static final long serialVersionUID = -1136702940620141852L;

	public ArrayList<String> topics;
    public int user_id;
    public int parent_id;
    public int number_parts;
    public int part_val;
    public int stance;
    public String text;

    public SubmitIdea(ArrayList<String> topics, int user_id, int parent_id, int number_parts, int part_val, int stance, String text) {
        this.topics = topics;
        this.user_id = user_id;
        this.parent_id = parent_id;
        this.number_parts = number_parts;
        this.stance = stance;
        this.part_val = part_val;
        this.text = text;
    }
}
