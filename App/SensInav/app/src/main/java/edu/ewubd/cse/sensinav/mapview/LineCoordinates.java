package edu.ewubd.cse.sensinav.mapview;

public class LineCoordinates {
    private float x;

    float x1 ,  y1, x2 ,  y2,z1,z2;
    private float y;

    public LineCoordinates(float x1 , float y1, float z1, float x2 , float y2, float z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public float getStartX() {
        return x1;
    }
    public float getStartY() {
        return y1;
    }
    public float getEndX() {
        return x2;
    }
    public float getEndY() {
        return y2;
    }
    public float  getStartZ(){return z1;}
    public float getEndZ(){return z2;}

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }


    public void setY(float y) {
        this.y = y;
    }
}
