package edu.ewubd.cse.sensinav.mapview;

public class Calculate {
    private float walkingThreshold = .8f;
    private float gyroscopeThreshold = 1f;
    private float walkingSteps;
    private float walkingStepDistance;
    private long previousWalkedTime;
    private static long minimum_step_per_second = 400;//ms
    public Calculate(float walkingThreshold , float gyroscopeThreshold){
        walkingStepDistance =0; // Should be in feet..
        walkingSteps = 0;
        this.gyroscopeThreshold = gyroscopeThreshold;
        this.walkingThreshold = walkingThreshold;
        previousWalkedTime = System.currentTimeMillis();
    }

    public boolean isDirectionOk(boolean direction , float gyroMagnitude){
            if(direction && gyroMagnitude<gyroscopeThreshold) return true;
            return false;
    }

    public void hasWalked(float accelerometerMagnitude , float gyroscopeMagnitude){
        long currentTime = System.currentTimeMillis();
        System.out.println(accelerometerMagnitude+"    "+gyroscopeMagnitude);
        if(accelerometerMagnitude>=walkingThreshold && gyroscopeMagnitude<gyroscopeThreshold && (currentTime-previousWalkedTime)>minimum_step_per_second){
            previousWalkedTime = currentTime;
            increamentDistance();
        }
    }

    public float getWalkingStepDistance(){
        return walkingStepDistance;
    }
    public void increamentDistance(){
        walkingSteps++;
        walkingStepDistance += MapInfo.getInstance().STEP_DISTANCE;
    }
    public float[] getNewCoordinate(float direction , float x1, float y1 , float z1){
        float x=x1,y=y1,z=z1;
        z = z1;
        //System.out.println("Direction: "+direction+" Distance: "+walkingDistance);
        float theta=0;
        if(direction>0 && direction<90){
            theta = 90-direction;
            x = ((float)Math.cos(Math.toRadians((double)theta))* walkingStepDistance)+x1;
            y = ((float)Math.sin(Math.toRadians((double)theta))* walkingStepDistance)+y1;
        }
        else if(direction>90 && direction<180){
            theta = 180-direction;
            x = ((float)Math.sin(Math.toRadians((double)theta))* walkingStepDistance)+x1;
            y = y1- ((float)Math.cos(Math.toRadians((double)theta))* walkingStepDistance);
        }
        else if(direction>180 && direction<270){
            theta = 270-direction;
            x = x1-((float)Math.cos(Math.toRadians((double)theta))* walkingStepDistance);
            y = y1- ((float)Math.sin(Math.toRadians((double)theta))* walkingStepDistance);
        }
        else if(direction>270 && direction<360){
            theta  = 360-direction;
            x = x1- ((float)Math.sin(Math.toRadians((double)theta))* walkingStepDistance);
            y = y1+ ((float)Math.cos(Math.toRadians((double)theta))* walkingStepDistance);
        }
        else if(direction==0){
            x = x1;
            y = y1+ walkingStepDistance;
        }
        else if(direction==90){
            x = x1+ walkingStepDistance;
            y = y1;
        }
        else if(direction==180){
            x = x1;
            y = y1- walkingStepDistance;
        }
        else if(direction==270){
            x = x1- walkingStepDistance;
            y = y1;
        }
        //System.out.println("x: "+x+" y: "+y+" z: "+z);
        return new float[]{x,y,z};
    }

}
