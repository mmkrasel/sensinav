package edu.ewubd.cse.sensinav.mapview;

import org.json.JSONArray;
import org.json.JSONException;

public class Node {
    private String label = "";
    private String building = "";
    private int id = 0;
    private double X = 0;
    private double Y = 0;
    private double Z = 0;

    public Node(String building, String label, int id, double x, double y, double z) {
        this.label = label;
        this.building = building;
        this.id = id;
        X = x;
        Y = y;
        Z = z;
    }

    public String getBuilding() {
        return building;
    }
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getID() {
        return id;
    }

    public void setID(int ID) {
        this.id= id;
    }

    public double getX() {
        return X;
    }

    public void setX(double x) {
        X = x;
    }

    public double getY() {
        return Y;
    }

    public void setY(double y) {
        Y = y;
    }

    public double getZ() {
        return Z;
    }

    public void setZ(double z) {
        Z = z;
    }
    public JSONArray getJSONArray(){
        JSONArray js = new JSONArray();
        js.put(id);
        js.put(building);
        js.put(label);
        try {
            js.put(X);
            js.put(Y);
            js.put(Z);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return js;
    }
}
