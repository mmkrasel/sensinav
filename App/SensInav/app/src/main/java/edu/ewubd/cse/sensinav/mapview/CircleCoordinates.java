package edu.ewubd.cse.sensinav.mapview;

public class CircleCoordinates {
    private float x;
    private float y;
    private float z;
    private float distance = 0;
    private String nodeName="";

    public CircleCoordinates(float x, float y, float z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.distance=0;
        this.nodeName = name;
    }

    public void setDistance(float dist){distance = dist;};


    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }
    public float getZ() {
        return z;
    }

    public String getNodeName(){return nodeName;}

    public void setY(float y) {
        this.y = y;
    }
}
