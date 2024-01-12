package edu.ewubd.cse.sensinav.sensors;

public class GyroscopeInfo {
        private float magnitude;
        private float[] gyroscope_value;
        private float[] dataBuffer;
        private int currentIndex,windowSize;
        public GyroscopeInfo(int windowSize){
            magnitude=0f;
            currentIndex=0;
            this.windowSize = windowSize;
            dataBuffer = new float[windowSize];
            gyroscope_value = new float[3];
        }
        public float getMagnitude(){
            return magnitude;
        }

        public void setGyroscope_value(float[] values){
            gyroscope_value = values.clone();
            magnitude = calculateMagnitude(gyroscope_value);
            magnitude = applyDirectionAverageFilter(magnitude);
        }

    public float applyDirectionAverageFilter(float gyroscopeValue) {
        dataBuffer[currentIndex] = gyroscopeValue;

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
}
