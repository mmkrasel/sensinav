package edu.ewubd.cse.sensinav;

import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import edu.ewubd.cse.sensinav.mapview.Edge;
import edu.ewubd.cse.sensinav.mapview.MapInfo;
import edu.ewubd.cse.sensinav.mapview.MyCanvas;
import edu.ewubd.cse.sensinav.mapview.Node;
import edu.ewubd.cse.sensinav.sensors.AccelerometerInfo;
import edu.ewubd.cse.sensinav.sensors.GyroscopeInfo;
import edu.ewubd.cse.sensinav.sensors.MagnetometerInfo;
import edu.ewubd.cse.sensinav.sensors.SensorService;
import edu.ewubd.cse.sensinav.util.Remote;
import edu.ewubd.cse.sensinav.util.Util;

public class NavigateShortestPathActivity extends AppCompatActivity {
    private float xAxis=0,yAxis=0,zAxis=0;
    private AccelerometerInfo accelerometerInfo;
    private MagnetometerInfo magnetometerInfo;
    private GyroscopeInfo gyroscopeInfo;
    private long previousWalkedTime = System.currentTimeMillis();
    private Intent serviceIntent;
    private float[] accelerometerValues = new float[3];
    private boolean isReceiverRegistered = false;
    private MyBroadcastReceiver accelerometer_receiver , gyroscope_receiver,magnetometer_receiver;
    private Node sourceNode, destinationNode;
    private MyCanvas myCanvas;
    private String selectedPath ="";
    private double selectedFloor = 0.0;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate_shortest_path);
        Intent intent = getIntent();
        int sourceNodeId = intent.getIntExtra("_FROM_DOOR_", -1);
        int destinationNodeId = intent.getIntExtra("_TO_DOOR_", -1);
        int sourceNodeIndex = MapInfo.getInstance().nodesIndices.get(sourceNodeId);
        int destinationNodeIndex = MapInfo.getInstance().nodesIndices.get(destinationNodeId);
        sourceNode = MapInfo.getInstance().nodeList.get(sourceNodeIndex);
        destinationNode = MapInfo.getInstance().nodeList.get(destinationNodeIndex);
        selectedFloor = intent.getDoubleExtra("_FLOOR_", 0.0);
    }
    @Override
    protected void onStart(){
        super.onStart();
        myCanvas = findViewById(R.id.myCanvas);
        findViewById(R.id.btnZoomIn).setOnClickListener(v -> myCanvas.zoomIn());
        findViewById(R.id.btnZoomOut).setOnClickListener(v -> myCanvas.zoomOut());
        this.showMapForFloor();
        this.fetchShortestPath();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(isReceiverRegistered){
            try {
                unregisterReceiver(accelerometer_receiver);
            }catch (Exception e){e.printStackTrace();}
            try {
                unregisterReceiver(magnetometer_receiver);
            }catch (Exception e){e.printStackTrace();}
            try {
                unregisterReceiver(gyroscope_receiver);
            }catch (Exception e){e.printStackTrace();}
            try {
                stopService(serviceIntent);
            }catch (Exception e){e.printStackTrace();}
        }
        isReceiverRegistered=false;
    }
    private void startTracking(){
        //System.out.println("###################################");
        //System.out.println("################# startTracking() ##################");
        //System.out.println("###################################");
        accelerometerInfo = new AccelerometerInfo(3);
        magnetometerInfo = new MagnetometerInfo(20);
        gyroscopeInfo = new GyroscopeInfo(3);
        registerSensorReceiver();
        serviceIntent = new Intent(this, SensorService.class);
        startService(serviceIntent);
    }
    private void designMap(){
        if(selectedPath.isEmpty()){
            System.out.println("Path is empty");
        }
        else {
            String[] nodesIds = selectedPath.split(",");
            ArrayList<Node> nodes = new ArrayList<>();
            for (String id : nodesIds) {
                int nodeIndex = MapInfo.getInstance().nodesIndices.get(Integer.parseInt(id));
                Node n = MapInfo.getInstance().nodeList.get(nodeIndex);
                nodes.add(n);
            }
            myCanvas.setNodesAtShortestPath(nodes);
        }
        xAxis = (float) sourceNode.getX();
        yAxis = (float) sourceNode.getY();
        zAxis = (float) sourceNode.getZ();
        runOnUiThread(() -> {
            myCanvas.setCoordinates(xAxis, yAxis, 0);
            startTracking();
        });
    }
    private void showMapForFloor() {
        ArrayList<Integer> floorNodeIds = new ArrayList<>();
        ArrayList<Node> floorNodes = new ArrayList<>();
        ArrayList<Edge> floorEdges = new ArrayList<>();
        double minX=99999999, maxX=-999999999, minY=999999999, maxY=-999999999;
        for(Edge e: MapInfo.getInstance().edgeList){
            Node n1 = MapInfo.getInstance().getNode(e.node1);
            Node n2 = MapInfo.getInstance().getNode(e.node2);
            //System.out.println(n.getZ());
            if(n1!=null && n2!=null && n1.getZ() == selectedFloor && n2.getZ() == selectedFloor){
                floorEdges.add(e);
                if(!floorNodeIds.contains(n1.getID())){
                    floorNodes.add(n1);
                    floorNodeIds.add(n1.getID());
                    if(minX > n1.getX()){
                        minX = n1.getX();
                    }
                    if(minY > n1.getY()){
                        minY = n1.getY();
                    }
                    if(maxX < n1.getX()){
                        maxX = n1.getX();
                    }
                    if(maxY < n1.getY()){
                        maxY = n1.getY();
                    }
                }
                if(!floorNodeIds.contains(n2.getID())){
                    floorNodes.add(n2);
                    floorNodeIds.add(n2.getID());
                    if(minX > n2.getX()){
                        minX = n2.getX();
                    }
                    if(minY > n2.getY()){
                        minY = n2.getY();
                    }
                    if(maxX < n2.getX()){
                        maxX = n2.getX();
                    }
                    if(maxY < n2.getY()){
                        maxY = n2.getY();
                    }
                }
            }
        }
        double mapWidth = maxX - minX;
        double mapHeight = maxY - minX;
        //System.out.println("@MapNodeActivity: "+floorNodes.size()+" "+MapInfo.getInstance().nodeList);
        //System.out.println(floorEdges);
        myCanvas.clearCanvas();
        myCanvas.setNodeData(floorNodes, floorEdges, mapWidth, mapHeight);
        myCanvas.invalidate();
        //myCanvas.zoomOut();
    }
    private boolean hasWalked(){
        //System.out.println("###################################");
        //System.out.println("################# hasWalked() ##################");
        //System.out.println("###################################");
        float accelerometerMagnitude = accelerometerInfo.getMagnitude();
        float  gyroscopeMagnitude = gyroscopeInfo.getMagnitude();
        long currentTime = System.currentTimeMillis();
        //System.out.println(accelerometerMagnitude+"    "+gyroscopeMagnitude);
        long minimum_step_per_second = 400;
        if(accelerometerMagnitude>=.8f && gyroscopeMagnitude<1f && (currentTime-previousWalkedTime)> minimum_step_per_second){
            previousWalkedTime = currentTime;
            return true;
        }
        return false;
    }
    private void calcCoordinate() {
        //System.out.println("###################################");
        //System.out.println("################# calcCoordinate() ##################");
        //System.out.println("###################################");
        float x=0,y=0,z=zAxis;
        float theta=0;
        float walkingStepDistance = MapInfo.getInstance().STEP_DISTANCE;
        float direction = magnetometerInfo.getCurrentDegree();
        if(direction>0 && direction<90){
            theta = 90-direction;
            x = ((float)Math.cos(Math.toRadians((double)theta))*walkingStepDistance)+xAxis;
            y = ((float)Math.sin(Math.toRadians((double)theta))*walkingStepDistance)+yAxis;
        }
        else if(direction>90 && direction<180){
            theta = 180-direction;
            x = ((float)Math.sin(Math.toRadians((double)theta))*walkingStepDistance)+xAxis;
            y = yAxis- ((float)Math.cos(Math.toRadians((double)theta))*walkingStepDistance);
        }
        else if(direction>180 && direction<270){
            theta = 270-direction;
            x = xAxis-((float)Math.cos(Math.toRadians((double)theta))*walkingStepDistance);
            y = yAxis- ((float)Math.sin(Math.toRadians((double)theta))*walkingStepDistance);
        }
        else if(direction>270 && direction<360){
            theta  = 360-direction;
            x = xAxis- ((float)Math.sin(Math.toRadians((double)theta))*walkingStepDistance);
            y = yAxis+ ((float)Math.cos(Math.toRadians((double)theta))*walkingStepDistance);
        }
        else if(direction==0){
            x = xAxis;
            y = yAxis+walkingStepDistance;
        }
        else if(direction==90){
            x = xAxis+walkingStepDistance;
            y = yAxis;
        }
        else if(direction==180){
            x = xAxis;
            y = yAxis-walkingStepDistance;
        }
        else if(direction==270){
            x = xAxis-walkingStepDistance;
            y = yAxis;
        }
        xAxis = x;
        yAxis =y;
        zAxis = z;
        //System.out.println("###################################");
        //System.out.println(x+"   "+y);
        //System.out.println("###################################");
        //System.out.println(direction);
        myCanvas.setCoordinates(xAxis, yAxis, direction);
    }
    private void processAccelerometerData(float[] val){
        //System.out.println("###################################");
        //System.out.println("################# processAccelerometerData() ##################");
        //System.out.println("###################################");
        //send for calculation..
        accelerometerValues = val.clone();
        accelerometerInfo.setAccelerometerValues(accelerometerValues);
        //System.out.println("ACC: "+ accelerometerInfo.getMagnitude());
        if(hasWalked() && !magnetometerInfo.isDirectionChanging()){
            //calculate and update the coordinate..
            //System.out.println("OK");
            calcCoordinate();
        } else if(magnetometerInfo.isDirectionChanging()){
            float direction = magnetometerInfo.getCurrentDegree();
            myCanvas.setCoordinates(xAxis, yAxis, direction);
        }
        //else System.out.println("Not OK");
        //calculate.hasWalked(accelerometerInfo.getMagnitude(),gyroscopeInfo.getMagnitude());
        //distanceShow.setText(String.valueOf(calculate.getWalkingDistance()));
    }
    private void processMagnetometerData(float[] val){
        float[] magnetometerValues = val.clone();
        magnetometerInfo.setMagnetometerLiveTracking(magnetometerValues,accelerometerValues);
        //System.out.println("MAG: "+ magnetometerInfo.getDirection());
//        if(!calculate.isDirectionOk(magnetometerInfo.isDirectionOk(),gyroscopeInfo.getMagnitude())){
//            //@Give warning of change of direction and restart again....
//            Toast.makeText(DataCollection.this, "DO NOT CHANGE DIRECTION... Start Again!!!", Toast.LENGTH_SHORT).show();
//        }
    }
    private void processGyroscopeData(float[] val){
        float[] gyroscopeValues = val.clone();
        gyroscopeInfo.setGyroscope_value(gyroscopeValues);
        //System.out.println("GYR: "+ gyroscopeInfo.getMagnitude());
    }
    private void registerSensorReceiver(){
        //System.out.println("###################################");
        //System.out.println("################# registerSensorReceiver() ##################");
        //System.out.println("###################################");
        isReceiverRegistered = true;
        accelerometer_receiver = new MyBroadcastReceiver();
        IntentFilter accelerometer_receiver_filter = new IntentFilter(SensorService.ACTION_ACCELEROMETER_DATA);
        registerReceiver(accelerometer_receiver, accelerometer_receiver_filter);
        magnetometer_receiver = new MyBroadcastReceiver();
        IntentFilter magnetometer_receiver_filter = new IntentFilter(SensorService.ACTION_MAGNETOMETER_DATA);
        registerReceiver(magnetometer_receiver, magnetometer_receiver_filter);
        gyroscope_receiver = new MyBroadcastReceiver();
        IntentFilter gyroscope_receiver_filter = new IntentFilter(SensorService.ACTION_GYROSCOPE_DATA);
        registerReceiver(gyroscope_receiver, gyroscope_receiver_filter);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //System.out.println("@MyBroadcastReceiver: "+intent.getAction());
            if (intent.getAction().equals(SensorService.ACTION_ACCELEROMETER_DATA)) {
                float[] floatArray = intent.getFloatArrayExtra(SensorService.EXTRA_ACCELEROMETER_DATA);
                processAccelerometerData(floatArray);
                //System.out.println(floatArray[0]+" acc ");
            }
            else if (intent.getAction().equals(SensorService.ACTION_MAGNETOMETER_DATA)) {
                float[] floatArray = intent.getFloatArrayExtra(SensorService.EXTRA_MAGNETOMETER_DATA);
                processMagnetometerData(floatArray);
                //System.out.println(floatArray[0]+" MAG ");
            }
            else if (intent.getAction().equals(SensorService.ACTION_GYROSCOPE_DATA)) {
                float[] floatArray = intent.getFloatArrayExtra(SensorService.EXTRA_GYROSCOPE_DATA);
                processGyroscopeData(floatArray);
                //System.out.println(floatArray[0]+" GYR ");
            }
        }
    }
    private void fetchShortestPath(){
        findViewById(R.id.progressCircle).setVisibility(View.VISIBLE);
        AsyncTask.execute(() -> {
            try {
                System.out.println("@SensInav: ********Fetching Shortest Path**********");
                JSONObject response = Remote.getInstance().httpRequest(Util.getInstance().getUrl()+"?action=shortest_path&from_node="+sourceNode.getID()+"&to_node="+destinationNode.getID(), null);
                if (response != null) {
                    try {
                        JSONObject data = response.getJSONObject("data");
                        if(data.has("path") && data.has("distance")) {
                            double distance = data.getDouble("distance");
                            if(distance > 0){
                                selectedPath = data.getString("path");
                                if(!selectedPath.isEmpty()){
                                    this.designMap();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                findViewById(R.id.progressCircle).setVisibility(View.GONE);
            });
        });
    }
}
