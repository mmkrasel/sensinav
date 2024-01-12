package edu.ewubd.cse.sensinav.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Util {
    private static final Util instance = new Util();
    private Util(){}
    //
    public static Util getInstance(){
        return instance;
    }

    private String HOME_URL = "https://muthosoft.com/univ/sensinav/";

    public void setUrl(String url){
        this.HOME_URL = url;
    }
    public String getUrl(){
        return this.HOME_URL;
    }
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    public float floorLabelToZ(String floorLabel){
        if(floorLabel.equalsIgnoreCase("Ground Floor")){
            return 1;
        }
        if(floorLabel.startsWith("Basement")){
            floorLabel = floorLabel.split(" ")[1];
            return -Float.parseFloat(floorLabel);
        }
        floorLabel = floorLabel.split(" ")[1];
        return Float.parseFloat(floorLabel);
    }
    public String zToFloorLabel(double z1){
        int z = (int) z1;
        if(z < 0){
            return "Basement "+(-z);
        } else if(z == 1){
            return "Ground Floor";
        } else {
            return "Floor "+z;
        }
    }
}
