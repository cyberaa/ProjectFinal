package common;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/23/13
 * Time: 7:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class IdeasNestedPack implements Serializable {


    private static final long serialVersionUID = -5664945419080484534L;

    public ArrayList<IdeaInfo> ideasNested;
    public byte[] attachFile;
    public int fileSize;

    public IdeasNestedPack(ArrayList<IdeaInfo> ideasNested, byte[] attachFile, int fileSize) {
        this.ideasNested = ideasNested;
        this.attachFile = attachFile;
        this.fileSize = fileSize;
    }
}
