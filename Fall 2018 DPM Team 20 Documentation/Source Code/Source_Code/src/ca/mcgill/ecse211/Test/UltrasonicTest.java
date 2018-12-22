package ca.mcgill.ecse211.Test;



import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
/**
 * We used this class to test our ultrasonic sensor and see the noise that it outputs
 * We also used to tune our track value in order to get accurate rotation of the robot
 * @author Group 21
 *
 */
 class UltrasonicTest {
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final Port usPort = LocalEV3.get().getPort("S1");
	
	private static SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
	private static SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provimdes samples from
	// this instance
	private static float[] usData = new float[usDistance.sampleSize()];
	private static final int  Rotate_Speed=100;
	private static Odometer odo;
	private static final double TRACK=14.45;
	private static final double WHEEL_RAD=2.2;
	private static final int FILTER_OUT = 20;
	public static void main (String[] args) throws OdometerExceptions {
		leftMotor.setSpeed(Rotate_Speed);
		rightMotor.setSpeed(Rotate_Speed);
		odo=Odometer.getOdometer(leftMotor, rightMotor,TRACK, WHEEL_RAD );
		Thread odoTh=new Thread(odo);
		odoTh.start();
		int distance;
		int filterControl=0;
		int trueDist;
		leftMotor.rotate(convertAngle(WHEEL_RAD,TRACK, 360), true);
		rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, 360), false);
	    while (odo.getXYT()[2]<359.0) {
	      usDistance.fetchSample(usData, 0); // acquire data
	      distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int 
	      if (distance >= 255 && filterControl < FILTER_OUT) {
	          // bad value, do not set the distance var, however do increment the
	          // filter value
	    	  distance=255;
	          trueDist= distance;
	          System.out.println(odo.getXYT()[2]+","+trueDist);
	          filterControl++;
	        } else if (distance >= 255) {
	          // We have repeated large values, so there must actually be nothing
	          // there: leave the distance alone
	          distance=255;
	          trueDist= distance;
	          System.out.println(odo.getXYT()[2]+","+trueDist);
	        } else {
	          // distance went below 255: reset filter and leave
	          // distance alone.
	          filterControl = 0;
	          trueDist = distance;
	          System.out.println(odo.getXYT()[2]+","+trueDist);
	         
	        }
	      
	      try {
	        Thread.sleep(100);
	      } catch (Exception e) {
	      } // Poor man's timed sampling
	    }
	}
	/**
	 * This method calculate the angle that must be passed to the motor 
	 * using the radius of the wheel and the distance we want the robot to cross
	 * @param radius
	 * @param distance
	 * @return	corresponding distance
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	/**
	 * This method calculate the angle that the motor must turn 
	 * in order for the robot to turn by an certain angle
	 * @param radius
	 * @param width
	 * @param angle
	 * @return Angle by which the wheels must turn
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
}

