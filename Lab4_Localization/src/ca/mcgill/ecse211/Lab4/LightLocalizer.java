package ca.mcgill.ecse211.Lab4;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
/**
 * This class is used to perform the light sensor localization 
 * @author Group 21
 *
 */
public class LightLocalizer {
	// Creating our light sensor and sample provider
	static Port portColor = LocalEV3.get().getPort("S4");
	static SensorModes myColor = new EV3ColorSensor(portColor);
	static SampleProvider myColorSample = myColor.getMode("Red");
	static float[] sampleColor = new float[myColor.sampleSize()];
	// creating variables and fields needed
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	private static final int ROTATE_SPEED = 100;
	private static final int FWDSPEED=200;
	private static double WHEEL_RAD;
	private static double TRACK;
	private Odometer odo;
	private static final int THRESHOLD=500;	// minimum at which we assume that w black line in detected
	private static final double sensor_dist=12.5; // distance between the light sensor and the wheels
	
	/**
	 * This is the class constructor
	 * @param leftMotor1
	 * @param rightMotor1
	 * @param odom
	 * @param rad
	 * @param track
	 */
	public LightLocalizer(EV3LargeRegulatedMotor leftMotor1, EV3LargeRegulatedMotor rightMotor1, Odometer odom, double rad, double track) {
		leftMotor = leftMotor1;
		rightMotor = rightMotor1;
		odo = odom;
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		WHEEL_RAD=rad;
		TRACK=track;
	}
	/**
	 * This method is used to perform the light sensor localization 
	 * @throws InterruptedException
	 */
	public void Localize() throws InterruptedException {
		// First we need to go closer to the (0,0)
		leftMotor.rotate(convertAngle(WHEEL_RAD,TRACK, 45), true);
		rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, 45), false);
		// Go straight until it detects a line
		leftMotor.setSpeed(FWDSPEED);
		rightMotor.setSpeed(FWDSPEED);
		leftMotor.forward();
		rightMotor.forward();
		int sensor_data;
		while(leftMotor.isMoving() || rightMotor.isMoving()) {
			sensor_data=fetch();
			if (sensor_data<THRESHOLD ) {
				break;
			}
		}
		
		leftMotor.rotate(convertDistance(WHEEL_RAD ,-(sensor_dist+5)), true);
		rightMotor.rotate(convertDistance(WHEEL_RAD ,-(sensor_dist+5)), false);
		leftMotor.rotate(-convertAngle(WHEEL_RAD,TRACK, 45), true);
		rightMotor.rotate(convertAngle(WHEEL_RAD, TRACK, 45), false);
		odo.setTheta(0);
		// make a 360deg rotation
		leftMotor.rotate(-convertAngle(WHEEL_RAD,TRACK, 360), true);
		rightMotor.rotate(+convertAngle(WHEEL_RAD, TRACK, 360), true);
		double [] angles =new double[4];
		int counter=0;
		
		// keep sampling while the robot is moving 
		// and record the values for the angles in corresponding slot in the array
		while(leftMotor.isMoving() || rightMotor.isMoving()) {
			
			sensor_data=fetch();
			if (sensor_data<THRESHOLD && counter<4) {
				Sound.beep();
				angles[counter]=odo.getXYT()[2];
				System.out.println(angles[counter]);
				counter++;
			}
			
		}
		// compute x and y using the formula fromt he slide
		double thetay=angles[0]-angles[2];
		double thetax=angles[1]-angles[3];
		System.out.println(thetay);
		System.out.println(thetax);
		double x=-sensor_dist*Math.cos(thetay*Math.PI/360);
		double y=-sensor_dist*Math.cos(thetax*Math.PI/360);
		System.out.println(x);
		System.out.println(y);
		// compute the angle to which we should turn to 
		// in order to go to (0,0)
		double theta=(Math.atan(x/y)*180/Math.PI);
		System.out.println(theta);
		// compute the distance that separate the robot from (0,0)
		double distance=Math.sqrt(x*x+y*y);
		System.out.println(distance);
		// compute the angle offset that results from US localization
		double fixtheta=angles[0]-(thetay/2)-270;
		System.out.println(fixtheta);
		// turn to the position (0,0)
		leftMotor.rotate(convertAngle(WHEEL_RAD,TRACK, theta+fixtheta), true);
		rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, theta+fixtheta), false);
		leftMotor.setSpeed(FWDSPEED);
		rightMotor.setSpeed(FWDSPEED);
		// travel to (0,0)
		leftMotor.rotate(convertDistance(WHEEL_RAD ,distance), true);
		rightMotor.rotate(convertDistance(WHEEL_RAD ,distance), false);
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		// make the robot turn back to 0 degrees 
	
		leftMotor.rotate(-convertAngle(WHEEL_RAD,TRACK, theta), true);
		rightMotor.rotate(convertAngle(WHEEL_RAD, TRACK, theta), false);
		System.out.println("done");
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
	/**
	 * This method is used to get data from the light sensor
	 * @return data from light sensor
	 * @throws InterruptedException
	 */
	private int fetch() throws InterruptedException {
		Thread.sleep(75);
		int sensor_data;
		myColorSample.fetchSample(sampleColor, 0);
		sensor_data=(int) (sampleColor[0] * 1000);
		
		return sensor_data;
	}
}
