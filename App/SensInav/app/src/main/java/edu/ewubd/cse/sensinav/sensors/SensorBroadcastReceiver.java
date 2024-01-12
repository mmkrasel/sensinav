package edu.ewubd.cse.sensinav.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SensorBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SensorService.ACTION_ACCELEROMETER_DATA)) {
            float[] floatArray = intent.getFloatArrayExtra(SensorService.EXTRA_ACCELEROMETER_DATA);
            Sensors.getInstance().processAccelerometerData(floatArray);
            //System.out.println(floatArray[0]+" acc ");
        }
        else if (intent.getAction().equals(SensorService.ACTION_MAGNETOMETER_DATA)) {
            float[] floatArray = intent.getFloatArrayExtra(SensorService.EXTRA_MAGNETOMETER_DATA);
            if(floatArray!=null) {
                Sensors.getInstance().processMagnetometerData(floatArray);
                //System.out.println(floatArray[0]+" MAG ");
            }
        }
        else if (intent.getAction().equals(SensorService.ACTION_GYROSCOPE_DATA)) {
            float[] floatArray = intent.getFloatArrayExtra(SensorService.EXTRA_GYROSCOPE_DATA);
            if(floatArray!=null) {
                Sensors.getInstance().processGyroscopeData(floatArray);
                //System.out.println(floatArray[0]+" GYR ");
            }
        }
    }
}
