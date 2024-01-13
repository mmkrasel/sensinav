package edu.ewubd.cse.sensinav.mapview;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import edu.ewubd.cse.sensinav.sensors.Sensors;
import edu.ewubd.cse.sensinav.util.CallBackListener;
import edu.ewubd.cse.sensinav.util.MapDB;
import edu.ewubd.cse.sensinav.util.Remote;
import edu.ewubd.cse.sensinav.util.Util;

public class MapInfo{
    private static final MapInfo instance = new MapInfo();
    private MapInfo(){}
    //
    public static MapInfo getInstance(){
        return instance;
    }
    ///////
    private MapDB mapDB;
    public int MAX_NODE_ID = 0;
    public float STEP_DISTANCE = 0.6f;
    public float STEP_THRESHOLD = 0.8f;
    public float GYROSCOPE_THRESHOLD = 1.0f;
    public String buildingName = "";
    public ArrayList<Node> nodeList;
    public HashMap<String, Integer> nodesMap = new HashMap<>();
    public HashMap<Integer, Integer> nodesIndices = new HashMap<>();
    public ArrayList<Edge> edgeList;
    public ArrayList<Integer> floorsList;
    private Context context;
    private boolean datasetLoading;
    private CallBackListener cbl;
    ///
    public void init(Context c){
        context = c;
        nodeList= new ArrayList<>();
        nodesMap = new HashMap<>();
        edgeList = new ArrayList<>();
        nodesIndices = new HashMap<>();
        floorsList = new ArrayList<>();
        mapDB = new MapDB(c);
    }
//    private void connectNodes(){
//        if(nodesMap!=null && nodesMap.containsKey(previous_node_name) && nodesMap.containsKey(current_nodeName)) {
//            double distance = Sensors.getInstance().getDistance();
//            int fromNode = nodesMap.get(previous_node_name);
//            int toNode = nodesMap.get(current_nodeName);
//            JSONObject postData = new JSONObject();
//            try {
//                postData.put("node1", nodeName1);
//                postData.put("node2", nodeName2);
//                postData.put("distance", );
//                //postData.put("node_z", Double.valueOf(z));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            new SaveEdge().execute(postData);
//        }
//    }
    public boolean isLoading(){
        return datasetLoading;
    }
    public Node getNode(int node) {
        Integer index = nodesIndices.get(node);
        if(index!=null){
            return nodeList.get(index);
        }
        return null;
    }
    public ArrayList<Node> getNodes(String label) {
        float z = Util.getInstance().floorLabelToZ(label);
        ArrayList<Node> nodes = new ArrayList<>();
        for(Node n:nodeList){
            if(n.getZ()==z){
                nodes.add(n);
            }
        }
        return nodes;
    }
    public void fetchMapInfo() {
        fetchNodesAndEdgesFromLocalDB();
        if(Util.getInstance().isNetworkAvailable(context)) {
            this.fetchNodesAndEdgesFromServer();
        } else {
            Toast.makeText(context, "No internet connection available", Toast.LENGTH_SHORT).show();
        }
    }
    private void resetMap(){
        nodeList.clear();
        nodesMap.clear();
        edgeList.clear();
        nodesIndices.clear();
        floorsList.clear();
        MAX_NODE_ID = 0;
        datasetLoading = false;
    }
    private void fetchNodesAndEdgesFromLocalDB(){
        try {
            System.out.println("@SensInav: ********Fetching all nodes from local DB **********");
            Cursor cur = mapDB.getAllNodes();
            if (cur != null) {
                resetMap();
                try {
                    while (cur.moveToNext()) {
                        int id = cur.getInt(0);
                        String building = cur.getString(1);
                        String label = cur.getString(2);
                        double nodeX = cur.getDouble(3);
                        double nodeY = cur.getDouble(4);
                        double nodeZ = cur.getDouble(5);
                        if (MAX_NODE_ID > id) {
                            MAX_NODE_ID = id;
                        }
                        nodesIndices.put(id, nodeList.size());
                        nodeList.add(new Node(building, label, id, nodeX, nodeY, nodeZ));
                        nodesMap.put(label, id);
                        int z = (int)nodeZ;
                        if(!floorsList.contains(z)){
                            floorsList.add(z);
                        }
                    }
                    cur.close();
                    cur = mapDB.getAllEdges();
                    if (cur != null) {
                        while (cur.moveToNext()) {
                            int node1 = cur.getInt(0);
                            int node2 = cur.getInt(1);
                            double distance = cur.getDouble(2);
                            edgeList.add(new Edge(node1, node2, distance));
                        }
                        cur.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "Failed to fetch node data", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void fetchNodesAndEdgesFromServer(){
        AsyncTask.execute(() -> {
            try {
                System.out.println("@SensInav: ********Fetching all data**********");
                datasetLoading = true;
                String url = Util.getInstance().getUrl()+"?action=init";
                if(!buildingName.isEmpty()){
                    url += "&building="+buildingName.replace(' ', '+');
                }
                JSONObject response = Remote.getInstance().httpRequest(url, null);
                if (response != null) {
                    try {
                        JSONObject data = response.getJSONObject("data");
                        if(data.has("nodes") && data.has("edges")) {
                            resetMap();
                            mapDB.truncate();
                            JSONArray nodes = data.getJSONArray("nodes");
                            // Iterate over the JSON array and add nodes to the nodeList
                            for (int i = 0; i < nodes.length(); i++) {
                                JSONObject nodeObject = nodes.getJSONObject(i);
                                String label = nodeObject.getString("label");
                                String building = nodeObject.getString("building");
                                int id = nodeObject.getInt("id");
                                double nodeX = nodeObject.getDouble("x");
                                double nodeY = nodeObject.getDouble("y");
                                double nodeZ = nodeObject.getDouble("z");
                                if (MAX_NODE_ID > id) {
                                    MAX_NODE_ID = id;
                                }
                                nodesIndices.put(id, nodeList.size());
                                nodeList.add(new Node(building, label, id, nodeX, nodeY, nodeZ));
                                nodesMap.put(label, id);
                                mapDB.insertNode(id, building, label, nodeX, nodeY, nodeZ);
                                int z = (int)nodeZ;
                                if(!floorsList.contains(z)){
                                    floorsList.add(z);
                                }
                            }
                            JSONArray edges = data.getJSONArray("edges");
                            for (int i = 0; i < edges.length(); i++) {
                                JSONObject edgeObject = edges.getJSONObject(i);
                                int node1 = edgeObject.getInt("node1");
                                int node2 = edgeObject.getInt("node2");
                                double distance = edgeObject.getDouble("distance");
                                edgeList.add(new Edge(node1, node2, distance));
                                mapDB.insertEdge(node1, node2, distance);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } //else {
                    //Toast.makeText(context, "Failed to fetch node data", Toast.LENGTH_SHORT).show();
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
            datasetLoading = false;
        });
    }
    public void createNode(String label , float x , float y , float z){
        ++MAX_NODE_ID;
        nodeList.add(new Node(buildingName, label, MAX_NODE_ID, x, y, z));
        nodesMap.put(label, MAX_NODE_ID);
        mapDB.insertNode(MAX_NODE_ID, buildingName, label, x, y, z);
        AsyncTask.execute(()  -> {
            try {
                String params = "action=create_node";
                params += "&id="+MAX_NODE_ID;
                params += "&x="+x;
                params += "&y="+y;
                params += "&z="+z;
                params += "&label="+ URLEncoder.encode(label, "UTF-8");
                if(!buildingName.isEmpty()) {
                    params += "&building=" + URLEncoder.encode(buildingName, "UTF-8");
                }
                Remote.getInstance().httpRequest(Util.getInstance().getUrl()+"?"+params, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public void createNewPath(float[] coordinate, String current_nodeName, String previous_node_name){
        //System.out.println(isNodeExist);
        if(nodesMap.containsKey(previous_node_name)){
            System.out.println("Previous Existed Node Not Found");
            ++MAX_NODE_ID;
            nodeList.add(new Node(buildingName, current_nodeName, MAX_NODE_ID, coordinate[0], coordinate[1], coordinate[2]));
            nodesMap.put(current_nodeName, MAX_NODE_ID);
        }
        Integer fromNode = nodesMap.get(previous_node_name);
        mapDB.insertNode(MAX_NODE_ID, buildingName, current_nodeName, coordinate[0], coordinate[1], coordinate[2]);
        mapDB.insertEdge(MAX_NODE_ID, fromNode, Sensors.getInstance().getDistance());
        if(nodesMap.containsKey(previous_node_name)) {
            AsyncTask.execute(() -> {
                try {
                    String params = "action=create_node";
                    params += "&id="+MAX_NODE_ID;
                    params += "&x="+coordinate[0];
                    params += "&y="+coordinate[0];
                    params += "&z="+coordinate[0];
                    params += "&label="+ URLEncoder.encode(current_nodeName, "UTF-8");
                    if(!buildingName.isEmpty()) {
                        params += "&building=" + URLEncoder.encode(buildingName, "UTF-8");
                    }
                    params += "&node2="+fromNode;
                    params += "&distance="+ Sensors.getInstance().getDistance();
                    Remote.getInstance().httpRequest(Util.getInstance().getUrl()+"?"+params, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
    public void syncData(CallBackListener cbl){
        this.cbl = cbl;
        if(Util.getInstance().isNetworkAvailable(context)) {
            JSONArray jsArrayNodes = new JSONArray();
            JSONArray jsArrayEdges = new JSONArray();
            for (Node n:nodeList){
                jsArrayNodes.put(n.getJSONArray());
            }
            for(Edge e: edgeList) {
                jsArrayEdges.put(e.getJSONArray());
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("nodes", jsArrayNodes);
                jsonObject.put("edges", jsArrayEdges);
                //System.out.println(jsonObject);
                AsyncTask.execute(()  -> {
                    String msg = "Done";
                    try {
                        Remote.getInstance().httpRequest(Util.getInstance().getUrl()+"?action=sync_map", jsonObject);
                    } catch (Exception e) {
                        msg = "Error while synchronizing";
                        e.printStackTrace();
                    }
                    if(this.cbl!=null) {
                        this.cbl.requestCallBack(msg);
                        this.cbl = null;
                    }
                });
            } catch (JSONException e) {
                if(this.cbl!=null) {
                    this.cbl.requestCallBack("Error to encode JSON");
                    this.cbl = null;
                }
                e.printStackTrace();
            }
        } else {
            if(this.cbl!=null) {
                this.cbl.requestCallBack("No internet connection available");
                this.cbl = null;
            }
            Toast.makeText(context, "No internet connection available", Toast.LENGTH_SHORT).show();
        }
    }
}
