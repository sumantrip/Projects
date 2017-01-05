import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by suman on 4/14/16.
 */
public class GraphNode {

    private String ID;
    private double lat, lon;
    private HashMap<String, String> attributes;
    private ArrayList<GraphNode> connectedNodes;

    public GraphNode() {
    }

    public GraphNode(String id, double latitude, double longitude) {
        ID = id;
        lat = latitude;
        lon = longitude;
        attributes = new HashMap<String, String>();
        connectedNodes = new ArrayList<>();
    }

    public ArrayList<GraphNode> getConnectedNodes() {
        return connectedNodes;
    }

    public String getNameFromAttributes() {
        return attributes.get("name");
    }

    public void insertKeyValue(String k, String v) {
        attributes.put(k, v);
    }

    public String getID() {
        return ID;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public boolean connected() {
        return connectedNodes.size() != 0;
    }

    public void connect(GraphNode other) {
        if (!connectedNodes.contains(other)) {
            connectedNodes.add(other);
            other.connect(this);
        }
    }

}

