package edu.ewubd.cse.sensinav.sensors;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

public class SensorService extends Service implements SensorEventListener {
    public static final String ACTION_ACCELEROMETER_DATA = "rijve.shovon.easygo.ACCELEROMETER_DATA";
    public static final String ACTION_MAGNETOMETER_DATA = "rijve.shovon.easygo.MAGNETOMETER_DATA";
    public static final String ACTION_GYROSCOPE_DATA = "rijve.shovon.easygo.GYROSCOPE_DATA";
    public static final String EXTRA_ACCELEROMETER_DATA = "extra_accelerometer_data";
    public static final String EXTRA_MAGNETOMETER_DATA = "extra_magnetometer_data";
    public static final String EXTRA_GYROSCOPE_DATA = "extra_gyroscope_data";
    private SensorManager sensorManager;
    private AccelerometerInfo accelerometerInfo;
    private Sensor accelerometerSensor,gyroscopeSensor;
    private Sensor magnetometerSensor;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerListeners();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterListeners();
    }
    private void registerListeners() {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private void unregisterListeners() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //System.out.println("@onSensorChanged Running");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //System.out.println(event.values[0]);
            Intent broadcastIntent = new Intent(SensorService.ACTION_ACCELEROMETER_DATA);
            broadcastIntent.putExtra(EXTRA_ACCELEROMETER_DATA, event.values); // Add any additional data you want to send
            sendBroadcast(broadcastIntent);
            //LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            Intent broadcastIntent = new Intent(SensorService.ACTION_MAGNETOMETER_DATA);
            broadcastIntent.putExtra(EXTRA_MAGNETOMETER_DATA, event.values); // Add any additional data you want to send
            sendBroadcast(broadcastIntent);
            //System.out.println("Ru");
        }
        else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            //System.out.println(event.values[0]);
            Intent broadcastIntent = new Intent(SensorService.ACTION_GYROSCOPE_DATA);
            broadcastIntent.putExtra(EXTRA_GYROSCOPE_DATA, event.values); // Add any additional data you want to send
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
