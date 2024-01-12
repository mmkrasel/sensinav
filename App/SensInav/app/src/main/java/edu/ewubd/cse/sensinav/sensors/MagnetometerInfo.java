package edu.ewubd.cse.sensinav.sensors;

import android.hardware.SensorManager;

public class MagnetometerInfo {
    private float fixed_direction=1000f;
    private float currentDegree=1000f;
    private float[] dataBuffer;  // Buffer to store accelerometer data
    private int windowSize;      // Size                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    of the moving average window
    private int currentIndex;

    public MagnetometerInfo(int windowSize){
        this.windowSize = windowSize;
        currentIndex = 0;
        dataBuffer = new float[windowSize];
    }

    public void setFixed_direction(float dir){
        fixed_direction = dir;
    }
    boolean isDirectionfixed(){
        if(fixed_direction<0) return false;
        return true;
    }

    public boolean isDirectionChanging(){
        if(Math.abs(currentDegree-dataBuffer[0])>15) {
            //System.out.println(fixed_direction+"---"+currentDegree);
            return true;
        }
        return false;
    }
    public void setMagnetometerLiveTracking(float[] magnetometerReading , float[] accelerometerValues){
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magnetometerReading);

        float[] orientationAngles = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        float azimuth = (float) Math.toDegrees(orientationAngles[0]);
        currentDegree = azimuth;
        dataBuffer[currentIndex] = currentDegree;
        currentIndex = (currentIndex + 1) % windowSize;
    }
    public void setMagnetometerReading(float[] magnetometerReading , float[] accelerometerValues){
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magnetometerReading);

        float[] orientationAngles = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        float azimuth = (float) Math.toDegrees(orientationAngles[0]);
        currentDegree = azimuth;
        currentDegree = applyDirectionAverageFilter(currentDegree);
        if(fixed_direction>900) fixed_direction = currentDegree;
    }
    public float applyDirectionAverageFilter(float magnetometerValue) {
        dataBuffer[currentIndex] = magnetometerValue;
        // Calculate moving average
        float sum = 0;
        for (int i = 0; i < windowSize; i++) {
            if(dataBuffer[i]==0) sum+=dataBuffer[currentIndex];
            else sum += dataBuffer[i];
        }
        float average = sum / windowSize;
        currentIndex = (currentIndex + 1) % windowSize;
        return average;
    }




    public float getDirection(){
        if (fixed_direction < 0) {
            return fixed_direction+360;
        }
        return fixed_direction;
    }

    public boolean isDirectionOk(){
        if(Math.abs(currentDegree-fixed_direction)>15) {
            //System.out.println(fixed_direction+"---"+currentDegree);
            return false;
        }
        return true;
    }

    public float getCurrentDegree(){
        if (currentDegree < 0) {
            return 360 + currentDegree;
        }
        return currentDegree;
    }
}
