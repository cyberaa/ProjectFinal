package common;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joaonuno
 * Date: 10/13/13
 * Time: 6:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class IdeaInfo implements Serializable
{
	private static final long serialVersionUID = 1470130151433907211L;

	private int idea_id;
    private String namealias;
    private String text;
    private int stance;

    /**
     *
     * @param idea_id
     * @param namealias
     * @param text
     * @param stance
     */
    public IdeaInfo(int idea_id, String namealias, String text, int stance) {
        this.idea_id = idea_id;
        this.namealias = namealias;
        this.text = text;
        this.stance = stance;
    }

    public int getIdeaId() {
        return this.idea_id;
    }

    public String getIdeaUser() {
        return this.namealias;
    }

    public String getIdeaText() {
        return this.text;
    }

    public int getIdeaStance() {
        return this.stance;
    }

    @Override
    public String toString() {
        return "IdeaInfo{" +
                "idea_id=" + idea_id +
                ", namealias='" + namealias + '\'' +
                ", text='" + text + '\'' +
                ", stance=" + stance +
                '}';
    }
}
