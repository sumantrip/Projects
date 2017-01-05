import java.io.File;
import java.util.ArrayList;

/**
 * Created by suman on 4/9/16.
 */
public class QuadTreeNode implements Comparable<QuadTreeNode> {

    private int imgNum, depth;
    private File image;
    private double ullat, ullon, lrlat, lrlon, lonDPP;
    private QuadTreeNode qt1, qt2, qt3, qt4;

    public QuadTreeNode(int img, double uplon, double uplat, double botlon, double botlat) {
        ullat = uplat;
        ullon = uplon;
        lrlat = botlat;
        lrlon = botlon;
        imgNum = img;
        depth = Integer.toString(img).length();
        lonDPP = (lrlon - ullon) / 256;

        if (imgNum == 0) {
            image = new File("root.png");
        } else {
            image = new File(Integer.toString(img) + ".png");
        }

        if (depth < 7) {
            qt1 = new QuadTreeNode(img * 10 + 1, ullon, ullat, (ullon + lrlon) / 2,
                    (ullat + lrlat) / 2);
            qt2 = new QuadTreeNode(img * 10 + 2, (ullon + lrlon) / 2, ullat, lrlon,
                    (ullat + lrlat) / 2);
            qt3 = new QuadTreeNode(img * 10 + 3, ullon, (ullat + lrlat) / 2,
                    (ullon + lrlon) / 2, lrlat);
            qt4 = new QuadTreeNode(img * 10 + 4, (ullon + lrlon) / 2,
                    (ullat + lrlat) / 2, lrlon, lrlat);
        } else {
            qt1 = null;
            qt2 = null;
            qt3 = null;
            qt4 = null;

        }
    }

    public ArrayList<QuadTreeNode> getChildren() {
        ArrayList<QuadTreeNode> kids = new ArrayList<QuadTreeNode>();
        if (qt1 != null) {
            kids.add(qt1);
            kids.add(qt2);
            kids.add(qt3);
            kids.add(qt4);
        }
        return kids;
    }

    public String getFileName() {
        return image.getName();
    }

    public double getLonDPP() {
        return lonDPP;
    }

    public double getUllat() {
        return ullat;
    }

    public double getUllon() {
        return ullon;
    }

    public double getLrlat() {
        return lrlat;
    }

    public double getLrlon() {
        return lrlon;
    }

    public int getFileNum() {
        return imgNum;
    }

    public int getDepth() {
        return depth;
    }
    /**
     *
     * @param n
     * @return int > 0 if this node comes before n
     *         int = 0 if this node is n
     *         int < 0 if n comes before this node
     */
    public int compareTo(QuadTreeNode n) {
        if (ullat > n.getUllat()) {
            return -1;
        } else if (ullat < n.getUllat()) {
            return 1;
        } else if (ullon < n.getUllon()) {
            return -1;
        } else if (ullon > n.getUllon()) {
            return 1;
        }
        return 0;
    }



}
