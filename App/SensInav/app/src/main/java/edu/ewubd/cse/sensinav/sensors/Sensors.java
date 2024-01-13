package edu.ewubd.cse.sensinav.sensors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;
import android.widget.Toast;

import edu.ewubd.cse.sensinav.mapview.Calculate;

public class Sensors {
    private static final Sensors instance = new Sensors();
    private Sensors() {}
    public static Sensors getInstance() {
        return instance;
    }
    ///
    private float[] gyroscopeValues = new float[3];
    private float[] magnetometerValues= new float[3];
    private float[] accelerometerValues = new float[3];
    private AccelerometerInfo accelerometerInfo;
    private MagnetometerInfo magnetometerInfo;
    private GyroscopeInfo gyroscopeInfo;
    private SensorBroadcastReceiver accelerometer_receiver , gyroscope_receiver,magnetometer_receiver;
    private boolean isReceiverRegistered = false;
    private Context context = null;
    private Intent serviceIntent = null;
    private Calculate calculate;
    private TextView tvLiveDistance;

    public void startSensors(){
        if(context != null) {
            calculate = new Calculate(MapInfo.getInstance().STEP_THRESHOLD,MapInfo.getInstance().GYROSCOPE_THRESHOLD);
            serviceIntent = new Intent(context, SensorService.class);
            context.startService(serviceIntent);
        }
    }
    public void stopSensors(){
        if(serviceIntent != null) {
            context.stopService(serviceIntent);
        }
    }
    public void register(Context context, TextView tvLiveDistance){
        this.context = context;
        this.tvLiveDistance = tvLiveDistance;

        accelerometerInfo = new AccelerometerInfo(3);
        magnetometerInfo = new MagnetometerInfo(5);
        gyroscopeInfo = new GyroscopeInfo(3);

        isReceiverRegistered=true;
        accelerometer_receiver = new SensorBroadcastReceiver();
        IntentFilter accelerometer_receiver_filter = new IntentFilter(SensorService.ACTION_ACCELEROMETER_DATA);
        context.registerReceiver(accelerometer_receiver, accelerometer_receiver_filter);
        magnetometer_receiver = new SensorBroadcastReceiver();
        IntentFilter magnetometer_receiver_filter = new IntentFilter(SensorService.ACTION_MAGNETOMETER_DATA);
        context.registerReceiver(magnetometer_receiver, magnetometer_receiver_filter);
        gyroscope_receiver = new SensorBroadcastReceiver();
        IntentFilter gyroscope_receiver_filter = new IntentFilter(SensorService.ACTION_GYROSCOPE_DATA);
        context.registerReceiver(gyroscope_receiver,gyroscope_receiver_filter);
    }
    public void unregister(){
        if(isReceiverRegistered){
            context.unregisterReceiver(accelerometer_receiver);
            context.unregisterReceiver(magnetometer_receiver);
            context.unregisterReceiver(gyroscope_receiver);
            if(serviceIntent!=null) {
                context.stopService(serviceIntent);
            }
        }
        isReceiverRegistered=false;
    }
    public void processAccelerometerData(float[] val){
        //send for calculation..
        if(magnetometerInfo.isDirectionOk()){
            accelerometerValues = val.clone();
            accelerometerInfo.setAccelerometerValues(accelerometerValues);
            calculate.hasWalked(accelerometerInfo.getMagnitude(),gyroscopeInfo.getMagnitude());
            String distance = ((int) (calculate.getWalkingStepDistance() * 10)) / 10.0 +"m";
            tvLiveDistance.setText(distance);
        }
    }
    public void processMagnetometerData(float[] val){
        magnetometerValues = val.clone();
        magnetometerInfo.setMagnetometerReading(magnetometerValues,accelerometerValues);
        if(!calculate.isDirectionOk(magnetometerInfo.isDirectionOk(),gyroscopeInfo.getMagnitude())){
            //@Give warning of change of direction and restart again....
            Toast.makeText(this.context, "DO NOT CHANGE DIRECTION... Start Again!!!", Toast.LENGTH_SHORT).show();
        }
    }
    public void processGyroscopeData(float[] val){
        gyroscopeValues = val.clone();
        gyroscopeInfo.setGyroscope_value(gyroscopeValues);
    }
    public float[] getNewCoordinate(float previous_node_x, float previous_node_y, float previous_node_z){
        //System.out.println("********"+(calculate==null));
        //System.out.println("********"+(magnetometerInfo==null));
        float[] details = calculate.getNewCoordinate(magnetometerInfo.getDirection(),previous_node_x,previous_node_y,previous_node_z);
        Toast.makeText(context, "X: " + details[0]+" Y: "+details[1]+" Z "+details[2]+" Dir: "+magnetometerInfo.getDirection(), Toast.LENGTH_SHORT).show();
        return details;
    }
    public double getDistance(){
        return calculate.getWalkingStepDistance();
    }
}
