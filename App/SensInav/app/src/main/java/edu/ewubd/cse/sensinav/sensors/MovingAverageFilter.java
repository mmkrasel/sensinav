package edu.ewubd.cse.sensinav.sensors;

public class MovingAverageFilter {
    private float[] dataBuffer;  // Buffer to store accelerometer data
    private int windowSize;      // Size of the moving average window
    private int currentIndex;    // Current index in the data buffer

    public MovingAverageFilter(int windowSize) {
        this.windowSize = windowSize;
        dataBuffer = new float[windowSize];
        currentIndex = 0;
    }

    public void resetValue(){
        for (int i = 0; i < windowSize; i++) {
            dataBuffer[i]=0;
        }
    }


    public float applyMovingAverageFilter(float CurerentValue) {
        dataBuffer[currentIndex] = CurerentValue;

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
}