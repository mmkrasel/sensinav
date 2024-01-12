package edu.ewubd.cse.sensinav.sensors;

public class HighPassFilter {
    //private float[] gravity;  // Gravity components
    private float alpha = .75f;      // Filtering factor
    private float[] filteredAcc;// Filtered accelerometer values
    private float[] gravity ; // Gravity Value

    public HighPassFilter() {
        filteredAcc = new float[3];
    }
    public void setGravity(float[] gravity_value_read_from_sensor){
        gravity = gravity_value_read_from_sensor.clone();
    }

    public void resetGravityValue(){
        gravity[0] = 0;
        gravity[1] = 0;
        gravity[2] = 0;
    }


    public float[] applyHighPassFilter(float[] accelerometerData) {
        // Apply high-pass filter to accelerometer data
        gravity[0] = alpha * gravity[0] + (1 - alpha) * accelerometerData[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * accelerometerData[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * accelerometerData[2];

        filteredAcc[0] = accelerometerData[0] - gravity[0];
        filteredAcc[1] = accelerometerData[1] - gravity[1];
        filteredAcc[2] = accelerometerData[2] - gravity[2];

        return filteredAcc;
    }
}