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
    public int parent_id;
    public int number_parts;
    public int part_val;
    public int stance;
    public String text;
    public String fileAttachName;

    public SubmitIdea(ArrayList<String> topics, int parent_id, int number_parts, int part_val, int stance, String text, String fileAttachName) {
        this.topics = topics;
        this.parent_id = parent_id;
        this.number_parts = number_parts;
        this.stance = stance;
        this.part_val = part_val;
        this.text = text;
        this.fileAttachName = fileAttachName;
    }
}
