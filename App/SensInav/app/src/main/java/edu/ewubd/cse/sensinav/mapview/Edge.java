package edu.ewubd.cse.sensinav.mapview;

import org.json.JSONArray;
import org.json.JSONException;

public class Edge {
    public int node1 = 0;
    public int node2 = 0;
    public double distance = 0;
    public Edge(int node1, int node2, double distance){
        this.node1 = node1;
        this.node2 = node2;
        this.distance = distance;
    }
    public JSONArray getJSONArray(){
        JSONArray js = new JSONArray();
        js.put(node1);
        js.put(node2);
        try {
            js.put(distance);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return js;
    }
}
