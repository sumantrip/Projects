import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.ArrayList;

/**
 *  Parses OSM XML files using an XML SAX parser. Used to construct the graph of roads for
 *  pathfinding, under some constraints.
 *  See OSM documentation on
 *  <a href="http://wiki.openstreetmap.org/wiki/Key:highway">the highway tag</a>,
 *  <a href="http://wiki.openstreetmap.org/wiki/Way">the way XML element</a>,
 *  <a href="http://wiki.openstreetmap.org/wiki/Node">the node XML element</a>,
 *  and the java
 *  <a href="https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html">SAX parser tutorial</a>.
 *  @author Alan Yao
 */
public class MapDBHandler extends DefaultHandler {
    /**
     * Only allow for non-service roads; this prevents going on pedestrian streets as much as
     * possible. Note that in Berkeley, many of the campus roads are tagged as motor vehicle
     * roads, but in practice we walk all over them with such impunity that we forget cars can
     * actually drive on them.
     */
    private static final Set<String> ALLOWED_HIGHWAY_TYPES = new HashSet<>(Arrays.asList
            ("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
                    "residential", "living_street", "motorway_link", "trunk_link", "primary_link",
                    "secondary_link", "tertiary_link"));
    private String activeState = "";
    private final GraphDB g;
    private GraphNode newNode;
    private ArrayList<GraphNode> nodesInWay;
    private final HashMap<String, GraphNode> map;
    private final HashMap<String, ArrayList<HashMap<String, Object>>> names;

    public MapDBHandler(GraphDB g) {
        this.g = g;
        map = new HashMap<String, GraphNode>();
        names = new  HashMap<String, ArrayList<HashMap<String, Object>>>();
        newNode = new GraphNode();
        nodesInWay = new ArrayList<GraphNode>();
    }

    /**
     * Called at the beginning of an element. Typically, you will want to handle each element in
     * here, and you may want to track the parent element.
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or
     *            if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace
     *                  processing is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are
     *              not available. This tells us which element we're looking at.
     * @param attributes The attributes attached to the element. If there are no attributes, it
     *                   shall be an empty Attributes object.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @see Attributes
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        /* Some example code on how you might begin to parse XML files. */
        if (qName.equals("node")) {
            activeState = "node";
            String id = attributes.getValue("id");
            double lat = Double.parseDouble(attributes.getValue("lat"));
            double lon = Double.parseDouble(attributes.getValue("lon"));
            newNode = new GraphNode(id, lat, lon);
            map.put(id, newNode);
        } else if (qName.equals("way")) {
            activeState = "way";
            nodesInWay.clear();
        } else if (activeState.equals("way") && qName.equals("tag")) {
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            if (k.equals("highway")) {
                if (ALLOWED_HIGHWAY_TYPES.contains(v)) {
                    for (int i = 0; i < nodesInWay.size() - 1; i++) {
                        nodesInWay.get(i).connect(nodesInWay.get(i + 1));
                    }
                }
            }
        } else if (activeState.equals("way") && qName.equals("nd")) {
            nodesInWay.add(map.get(attributes.getValue("ref")));
        }  else if (activeState.equals("node") && qName.equals("tag")) {
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            newNode.insertKeyValue(k, v);
            if (k.equals("name")) {
                addToNames(newNode);
            }
        }
    }

    private void addToNames(GraphNode n) {
        HashMap<String, Object> hash = new HashMap<>();
        hash.put("lat", newNode.getLat());
        hash.put("lon", newNode.getLon());
        hash.put("id", Long.parseLong(newNode.getID()));
        hash.put("name", newNode.getNameFromAttributes());
        String name = n.getNameFromAttributes();
        name = GraphDB.cleanString(name);
        for (int i = 1; i <= name.length(); i++) {
            String namePrefix = name.substring(0, i);
            if (names.containsKey(namePrefix)) {
                names.get(namePrefix).add(hash);
            } else {
                ArrayList<HashMap<String, Object>> a = new ArrayList<>();
                a.add(hash);
                names.put(namePrefix, a);
            }
        }
    }

    public HashMap<String, GraphNode> getMap() {
        return map;
    }


    /**
     * Receive notification of the end of an element. You may want to take specific terminating
     * actions here, like finalizing vertices or edges found.
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or
     *            if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace
     *                  processing is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are
     *              not available.
     * @throws SAXException  Any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            activeState = "";
            newNode = null;
            nodesInWay.clear();
        }
    }

    public HashMap<String, ArrayList<HashMap<String, Object>>> getNames() {
        return names;
    }
}
