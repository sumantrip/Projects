import java.util.Collections;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.util.List;
import java.util.PriorityQueue;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.awt.Graphics;
import java.util.Set;
import java.util.ArrayList;
import java.awt.Graphics2D;
import java.awt.BasicStroke;


/* Maven is used to pull in these dependencies. */
import com.google.gson.Gson;

import javax.imageio.ImageIO;

import static spark.Spark.*;

/**
 * This MapServer class is the entry point for running the JavaSpark web server for the BearMaps
 * application project, receiving API calls, handling the API call processing, and generating
 * requested images and routes.
 * @author Alan Yao
 */
public class MapServer {
    /**
     * The root upper left/lower right longitudes and latitudes represent the bounding box of
     * the root tile, as the images in the img/ folder are scraped.
     * Longitude == x-axis; latitude == y-axis.
     */
    public static final double ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
            ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;
    /** Each tile is 256x256 pixels. */
    public static final int TILE_SIZE = 256;
    /** HTTP failed response. */
    private static final int HALT_RESPONSE = 403;
    /** Route stroke information: typically roads are not more than 5px wide. */
    public static final float ROUTE_STROKE_WIDTH_PX = 5.0f;
    /** Route stroke information: Cyan with half transparency. */
    public static final Color ROUTE_STROKE_COLOR = new Color(108, 181, 230, 200);
    /** The tile images are in the IMG_ROOT folder. */
    private static final String IMG_ROOT = "img/";

    private static QuadTree quad;

    private static LinkedList<Long> routeNodesByID = new LinkedList<>();

    /**
     * The OSM XML file path. Downloaded from <a href="http://download.bbbike.org/osm/">here</a>
     * using custom region selection.
     **/
    private static final String OSM_DB_PATH = "berkeley.osm";
    /**
     * Each raster request to the server will have the following parameters
     * as keys in the params map accessible by,
     * i.e., params.get("ullat") inside getMapRaster(). <br>
     * ullat -> upper left corner latitude,<br> ullon -> upper left corner longitude, <br>
     * lrlat -> lower right corner latitude,<br> lrlon -> lower right corner longitude <br>
     * w -> user viewport window width in pixels,<br> h -> user viewport height in pixels.
     **/
    private static final String[] REQUIRED_RASTER_REQUEST_PARAMS = {"ullat", "ullon", "lrlat",
        "lrlon", "w", "h"};
    /**
     * Each route request to the server will have the following parameters
     * as keys in the params map.<br>
     * start_lat -> start point latitude,<br> start_lon -> start point longitude,<br>
     * end_lat -> end point latitude, <br>end_lon -> end point longitude.
     **/
    private static final String[] REQUIRED_ROUTE_REQUEST_PARAMS = {"start_lat", "start_lon",
        "end_lat", "end_lon"};
    /* Define any static variables here. Do not define any instance variables of MapServer. */
    private static GraphDB g;

    /**
     * Place any initialization statements that will be run before the server main loop here.
     * Do not place it in the main function. Do not place initialization code anywhere else.
     * This is for testing purposes, and you may fail tests otherwise.
     **/
    public static void initialize() {
        quad = new QuadTree(new File(IMG_ROOT + "root.png"), ROOT_ULLON, ROOT_ULLAT,
                ROOT_LRLON, ROOT_LRLAT);
        g = new GraphDB(OSM_DB_PATH);
    }

    public static void main(String[] args) {
        initialize();
        staticFileLocation("/page");
        /* Allow for all origin requests (since this is not an authenticated server, we do not
         * care about CSRF).  */
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });

        /* Define the raster endpoint for HTTP GET requests. I use anonymous functions to define
         * the request handlers. */
        get("/raster", (req, res) -> {
            HashMap<String, Double> params =
                    getRequestParams(req, REQUIRED_RASTER_REQUEST_PARAMS);
            /* The png image is written to the ByteArrayOutputStream */
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            /* getMapRaster() does almost all the work for this API call */
            Map<String, Object> rasteredImgParams = getMapRaster(params, os);
            /* On an image query success, add the image data to the response */
            if (rasteredImgParams.containsKey("query_success")
                    && (Boolean) rasteredImgParams.get("query_success")) {
                String encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
                rasteredImgParams.put("b64_encoded_image_data", encodedImage);
            }
            /* Encode response to Json */
            Gson gson = new Gson();
            return gson.toJson(rasteredImgParams);
        });

        /* Define the routing endpoint for HTTP GET requests. */
        get("/route", (req, res) -> {
            HashMap<String, Double> params =
                    getRequestParams(req, REQUIRED_ROUTE_REQUEST_PARAMS);
            LinkedList<Long> route = findAndSetRoute(params);
            //********DRAW******
            return !route.isEmpty();
        });

        /* Define the API endpoint for clearing the current route. */
        get("/clear_route", (req, res) -> {
            clearRoute();
            return true;
        });

        /* Define the API endpoint for search */
        get("/search", (req, res) -> {
            Set<String> reqParams = req.queryParams();
            String term = req.queryParams("term");
            Gson gson = new Gson();
            /* Search for actual location data. */
            if (reqParams.contains("full")) {
                List<Map<String, Object>> data = getLocations(term);
                return gson.toJson(data);
            } else {
                /* Search for prefix matching strings. */
                List<String> matches = getLocationsByPrefix(term);
                return gson.toJson(matches);
            }
        });

        /* Define map application redirect */
        get("/", (request, response) -> {
            response.redirect("/map.html", 301);
            return true;
        });
    }

    /**
     * Validate & return a parameter map of the required request parameters.
     * Requires that all input parameters are doubles.
     * @param req HTTP Request
     * @param requiredParams TestParams to validate
     * @return A populated map of input parameter to it's numerical value.
     */
    private static HashMap<String, Double> getRequestParams(
            spark.Request req, String[] requiredParams) {
        Set<String> reqParams = req.queryParams();
        HashMap<String, Double> params = new HashMap<>();
        for (String param : requiredParams) {
            if (!reqParams.contains(param)) {
                halt(HALT_RESPONSE, "Request failed - parameters missing.");
            } else {
                try {
                    params.put(param, Double.parseDouble(req.queryParams(param)));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    halt(HALT_RESPONSE, "Incorrect parameters - provide numbers.");
                }
            }
        }
        return params;
    }


    /**
     * Handles raster API calls, queries for tiles and rasters the full image. <br>
     * <p>
     *     The rastered photo must have the following properties:
     *     <ul>
     *         <li>Has dimensions of at least w by h, where w and h are the user viewport width
     *         and height.</li>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *         <li>If a current route exists, lines of width ROUTE_STROKE_WIDTH_PX and of color
     *         ROUTE_STROKE_COLOR are drawn between all nodes on the route in the rastered photo.
     *         </li>
     *     </ul>
     *     Additional image about the raster is returned and is to be included in the Json response.
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query bounding box and
     *               the user viewport width and height.
     * @param os     An OutputStream that the resulting png image should be written to.
     * @return A map of parameters for the Json response as specified:
     * "raster_ul_lon" -> Double, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Double, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Double, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Double, the bounding lower right latitude of the rastered image <br>
     * "raster_width"  -> Double, the width of the rastered image <br>
     * "raster_height" -> Double, the height of the rastered image <br>
     * "depth"         -> Double, the 1-indexed quadtree depth of the nodes of the rastered image.
     * Can also be interpreted as the length of the numbers in the image string. <br>
     * "query_success" -> Boolean, whether an image was successfully rastered. <br>
     * @see #REQUIRED_RASTER_REQUEST_PARAMS
     */
    public static Map<String, Object> getMapRaster(Map<String, Double> params, OutputStream os) {
        HashMap<String, Object> rasteredImageParams = new HashMap<>();
        ArrayList<QuadTreeNode> imageNodes = new ArrayList<QuadTreeNode>();

        if (params.get("ullon") < ROOT_ULLON || params.get("ullat") > ROOT_ULLAT
                || params.get("lrlon") > ROOT_LRLON || params.get("lrlat") < ROOT_LRLAT) {
            rasteredImageParams.put("query_success", false);
            return rasteredImageParams;
        } else {
            double lonDPP = (params.get("lrlon") - params.get("ullon")) / params.get("w");
            quad.getImageWithRightDPP(quad.getRoot(), imageNodes, 0, lonDPP, params.get("ullon"),
                    params.get("ullat"), params.get("lrlon"), params.get("lrlat"));
            Collections.sort(imageNodes);
            QuadTreeNode first = imageNodes.get(0);
            QuadTreeNode last = imageNodes.get(imageNodes.size() - 1);


            int width = Math.abs(TILE_SIZE * (int) Math.round((first.getUllon() - last.getLrlon())
                    / (first.getUllon() - first.getLrlon())));
            int height = Math.abs(TILE_SIZE * (int) Math.round((first.getUllat() - last.getLrlat())
                    / (first.getUllat() - first.getLrlat())));

            rasteredImageParams.put("raster_ul_lon", first.getUllon());
            rasteredImageParams.put("raster_ul_lat", first.getUllat());
            rasteredImageParams.put("raster_lr_lon", last.getLrlon());
            rasteredImageParams.put("raster_lr_lat", last.getLrlat());
            rasteredImageParams.put("raster_width", width);
            rasteredImageParams.put("raster_height", height);
            rasteredImageParams.put("query_success", true);
            rasteredImageParams.put("depth", first.getDepth());

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics graph = result.getGraphics();
            int x = 0;
            int y = 0;
            for (QuadTreeNode image : imageNodes) {
                BufferedImage bi = null;
                try {
                    bi = ImageIO.read(new File(IMG_ROOT + image.getFileName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (x >= result.getWidth()) {
                    x = 0;
                    y += bi.getHeight();
                }

                graph.drawImage(bi, x, y, null);
                x += bi.getWidth();
            }

            if (!routeNodesByID.isEmpty()) {
                drawRoute(result, rasteredImageParams);
            }
            try {
                ImageIO.write(result, "png", os);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return rasteredImageParams;
        }
    }

    /**
     * Searches for the shortest route satisfying the input request parameters, sets it to be the
     * current route, and returns a <code>LinkedList</code> of the route's node ids for testing
     * purposes. <br>
     * The route should start from the closest node to the start point and end at the closest node
     * to the endpoint. Distance is defined as the euclidean between two points (lon1, lat1) and
     * (lon2, lat2).
     * @param params from the API call described in REQUIRED_ROUTE_REQUEST_PARAMS
     * @return A LinkedList of node ids from the start of the route to the end.
     */
    public static LinkedList<Long> findAndSetRoute(Map<String, Double> params) {
        clearRoute();
        double startLon = params.get("start_lon");
        double startLat = params.get("start_lat");
        double endLon = params.get("end_lon");
        double endLat = params.get("end_lat");

        PriorityQueue<ANode> pq = new PriorityQueue<>();
        HashMap<Long, ANode> visited = new HashMap<>();
        GraphNode startNode = getNearestNode(startLon, startLat);
        GraphNode endNode = getNearestNode(endLon, endLat);
        ANode start = new ANode(null, endNode, startNode);
        pq.add(start);

        while (pq.peek().getNode().getID() != endNode.getID()) {
            ANode temp = pq.remove();
            Iterable<GraphNode> connected = temp.getNode().getConnectedNodes();
            for (GraphNode gn : connected) {
                ANode newNode = new ANode(temp, endNode, gn);
                Long id = Long.parseLong(gn.getID());
                if (!visited.containsKey(id)) {
                    visited.put(id, newNode);
                    pq.add(newNode);
                } else if (newNode.getPriority() < visited.get(id).getPriority()) {
                    pq.add(newNode);
                    visited.put(id, newNode);
                }
            }
        }
        ANode node = pq.peek();
        while (node != null) {
            routeNodesByID.add(0, Long.parseLong(node.getNode().getID()));
            node = node.getParent();
        }
        return routeNodesByID;
    }

    private static GraphNode getNearestNode(double lon, double lat) {
        GraphNode currentClosest = null;
        double latDist, lonDist, actualDist;
        double minDist = 1000000000;
        for (GraphNode n : g.getMaphandler().getMap().values()) {
            latDist = Math.abs(n.getLat() - lat);
            lonDist = Math.abs(n.getLon() - lon);
            actualDist = Math.sqrt(lonDist * lonDist + latDist * latDist);
            if (actualDist < minDist) {
                minDist = actualDist;
                currentClosest = n;
            }
        }
        return currentClosest;
    }

    private static void drawRoute(BufferedImage bi, HashMap<String, Object> hash) {
        HashMap<String, GraphNode> nodes = g.getMaphandler().getMap();
        Graphics2D painter = (Graphics2D) bi.getGraphics();
        BasicStroke bs = new BasicStroke(MapServer.ROUTE_STROKE_WIDTH_PX, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND);
        painter.setColor(ROUTE_STROKE_COLOR);
        painter.setStroke(bs);

        double ullon = (double) hash.get("raster_ul_lon");
        double ullat = (double) hash.get("raster_ul_lat");
        double lrlon = (double) hash.get("raster_lr_lon");
        double lrlat = (double) hash.get("raster_lr_lat");

        int width = (int) hash.get("raster_width");
        int height = (int) hash.get("raster_height");
        double totalLon = Math.abs(ullon - lrlon);
        double totalLat = Math.abs(ullat - lrlat);
        GraphNode prev = nodes.get(Long.toString(routeNodesByID.get(0)));

        for (Long num : routeNodesByID.subList(1, routeNodesByID.size())) {
            String s = Long.toString(num);
            GraphNode curr = nodes.get(s);
            int x1 = (int) (((prev.getLon() - ullon) / totalLon) * width);
            int y1 = (int) (((ullat - prev.getLat()) / totalLat) * height);
            int x2 = (int) (((curr.getLon() - ullon) / totalLon) * width);
            int y2 = (int) (((ullat - curr.getLat()) / totalLat) * height);
            painter.drawLine(x1, y1, x2, y2);
            prev = curr;
        }



    }
    /**
     * Clear the current found route, if it exists.
     */
    public static void clearRoute() {
        routeNodesByID.clear();
    }

    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public static List<String> getLocationsByPrefix(String prefix) {
        prefix = g.cleanString(prefix);
        ArrayList<HashMap<String, Object>> a = g.getMaphandler().getNames().get(prefix);
        List<String> names = new LinkedList<>();
        for (HashMap<String, Object> h : a) {
            names.add((String) h.get("name"));
        }
        return names;
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public static List<Map<String, Object>> getLocations(String locationName) {
        String cleaned = g.cleanString(locationName);
        HashMap<String, ArrayList<HashMap<String, Object>>> hash = g.getMaphandler().getNames();
        List<Map<String, Object>> vals = new LinkedList<>();
        for (HashMap<String, Object> h : hash.get(cleaned)) {
            if (cleaned.equals(GraphDB.cleanString((String) (h.get("name"))))) {
                vals.add(h);
            }
        }
        return vals;
    }
}
