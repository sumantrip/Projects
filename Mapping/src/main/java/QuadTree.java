import java.io.File;
import java.util.ArrayList;

/**
 * Created by suman on 4/9/16.
 */
public class QuadTree {

    private QuadTreeNode root;


    public QuadTree(File i, double rootullon, double rootullat,
                    double rootlrlon, double rootlrlat) {
        root = new QuadTreeNode(0, rootullon, rootullat, rootlrlon, rootlrlat);
    }

    public QuadTreeNode getRoot() {
        return root;
    }

    public void getImageWithRightDPP(QuadTreeNode node,
                                     ArrayList<QuadTreeNode> selected, int depth, double lonDPP,
                                     double ullon, double ullat, double lrlon, double lrlat) {
        if (Math.abs(node.getLonDPP()) <= Math.abs(lonDPP) || node.getDepth() == 7) {
            if (inQueryBox(node, ullon, ullat, lrlon, lrlat)) {
                selected.add(node);
            }
        } else {
            for (QuadTreeNode q : node.getChildren()) {
                if (inQueryBox(q, ullon, ullat, lrlon, lrlat)) {
                    getImageWithRightDPP(q, selected, depth + 1, lonDPP,
                            ullon, ullat, lrlon, lrlat);
                }
            }
        }
    }

    private boolean inQueryBox(QuadTreeNode q, double ullon, double ullat,
                               double lrlon, double lrlat) {
        return (q.getLrlat() < ullat && q.getUllon() < lrlon
                && q.getUllat() > lrlat && q.getLrlon() > ullon);
    }


}
