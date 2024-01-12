package edu.ewubd.cse.sensinav.sensors;

public class AccelerometerInfo {
    private float[] accelerometerValues;
    private float magnitude;
    private float alpha = .75f;
    private float[] gravityValues;
    private float[] dataBuffer;
    private int currentIndex,windowSize;
    public AccelerometerInfo(int windowSize){
        this.windowSize = windowSize;
        currentIndex=0;
        accelerometerValues = new float[3];
        gravityValues = new float[3];
        dataBuffer = new float[windowSize];
        magnitude = 0f;
    }

    public void setAccelerometerValues(float[] event){
        accelerometerValues=event.clone();
        if(gravityValues[0]==0 && gravityValues[1]==0 && gravityValues[2]==0){
            gravityValues = accelerometerValues.clone();
        }
        accelerometerValues = applyHighPassFilter(accelerometerValues);
        magnitude = calculateMagnitude(accelerometerValues);
        magnitude = applyMovingAverageFilter();
    }

    public float getMagnitude(){
        return magnitude;
    }

    public float[] applyHighPassFilter(float[] accelerometerData) {
        // Apply high-pass filter to accelerometer data
        gravityValues[0] = alpha * gravityValues[0] + (1 - alpha) * accelerometerData[0];
        gravityValues[1] = alpha * gravityValues[1] + (1 - alpha) * accelerometerData[1];
        gravityValues[2] = alpha * gravityValues[2] + (1 - alpha) * accelerometerData[2];

        accelerometerData[0] = accelerometerData[0] - gravityValues[0];
        accelerometerData[1] = accelerometerData[1] - gravityValues[1];
        accelerometerData[2] = accelerometerData[2] - gravityValues[2];

        return accelerometerData;
    }
    public float applyMovingAverageFilter() {
        dataBuffer[currentIndex] = magnitude;

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
    private float calculateMagnitude(float[] vector){
        float sum = 0;

        for (float value : vector) {
            sum += value * value;
        }
        return  (float) Math.sqrt(sum);
    }

    public float[] getAccelerometerValues(){
        System.out.println(accelerometerValues[0]);
        return accelerometerValues;
    }

}
