/**
 * Created by suman on 4/15/16.
 */
public class ANode implements Comparable<ANode> {

    private GraphNode node, end;
    private ANode parent;
    private double distFromStart, heuristic, priority;

    public ANode(ANode parentNode, GraphNode endNode, GraphNode currNode) {
        parent = parentNode;
        end = endNode;
        node = currNode;
        distFromStart = calcDistFromStart();
        heuristic = calcHeuristic();
        priority = calcPriority();
    }

    public double getPriority() {
        return priority;
    }

    public ANode getParent() {
        return parent;
    }

    public double calcHeuristic() {
        double latFromEnd = Math.abs(node.getLat() - end.getLat());
        double lonFromEnd = Math.abs(node.getLon() - end.getLon());
        return Math.sqrt(latFromEnd * latFromEnd + lonFromEnd * lonFromEnd);
    }

    public double calcDistFromStart() {
        if (parent == null) {
            return 0;
        }
        double latFromParent = Math.abs(node.getLat() - parent.getNode().getLat());
        double lonFromParent = Math.abs(node.getLon() - parent.getNode().getLon());
        double distFromParent = Math.sqrt(latFromParent * latFromParent
                + lonFromParent * lonFromParent);
        return distFromParent + parent.distFromStart;
    }

    public double calcPriority() {
        return distFromStart + heuristic;
    }

    public GraphNode getNode() {
        return node;
    }

    @Override
    public int compareTo(ANode other) {
        if (this.priority < other.priority) {
            return -1;
        } else if (this.priority == other.priority) {
            return 0;
        }
        return 1;
    }
}
