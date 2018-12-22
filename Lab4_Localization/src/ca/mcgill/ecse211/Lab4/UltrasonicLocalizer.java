package ca.mcgill.ecse211.Lab4;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**
 * This class is used to perform the ultrasonic localization
 * 
 * @author Group 21
 *
 */
public class UltrasonicLocalizer {
	// Declare the variables and fields needed
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	private static final int dist = 35;
	private static final int ROTATE_SPEED = 100;
	private static double WHEEL_RAD;
	private static double TRACK;
	private Odometer odo;
	private static final int SAMPLING_PERIOD = 100;
	private static final int FILTER_OUT = 10;
	// intiate the ultrasonic sensor and the sample provider
	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
	private static SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
	// this instance
	private static float[] usData = new float[usDistance.sampleSize()];

	/**
	 * This our class constructor
	 * 
	 * @param leftMotor1
	 * @param rightMotor1
	 * @param odom
	 * @param rad
	 * @param track
	 */
	public UltrasonicLocalizer(EV3LargeRegulatedMotor leftMotor1, EV3LargeRegulatedMotor rightMotor1, Odometer odom,
			double rad, double track) {
		leftMotor = leftMotor1;
		rightMotor = rightMotor1;
		odo = odom;
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		WHEEL_RAD = rad;
		TRACK = track;
	}

	/**
	 * This is our FallingEdge method It is used to perform the falling edge localization
	 */
	public void FallingEdge() {
		int distance = 0;
		double alpha, beta;
		leftMotor.forward();
		rightMotor.backward();
		// To make sure that the robot is not facing the wall
		// we turn right until it doesn't see it anymore
		while (distance < dist) {
			distance = fetch();

		}
		// turn right until the robot detect a falling edge
		while (distance > dist) {
			distance = fetch();

		}
		// record the angle and switch directions
		Sound.beep();
		alpha = odo.getXYT()[2];
		System.out.println(alpha);

		rightMotor.forward();
		leftMotor.backward();
		// making sure the robot doesn't detect the same falling edge
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}
		distance = 255;
		// turn left until the robot detect a falling edge again
		while (distance > dist) {
			distance = fetch();

		}
		// record the second angle
		Sound.beep();
		beta = odo.getXYT()[2];

		leftMotor.stop(true);
		rightMotor.stop();

		System.out.println(beta);
		// compute the average of the two angles detected
		double avr = (alpha + beta) / 2.0;
		System.out.println(avr);
		double dtheta;
		// calculate the change in theta by which the robot should rotate in order to
		// be facing 0deg
		if (alpha < beta) {
			dtheta =35-avr;
		} else {
			dtheta = 225 - avr;
		}

		System.out.println(dtheta);
		dtheta += odo.getXYT()[2];
		// rotate by the calculated amount
		leftMotor.rotate(convertAngle(WHEEL_RAD, TRACK, dtheta), true);
		rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, dtheta), false);

	}
	/**
	 * This is our RisingEdge method It is used to perform the rising edge localization
	 */
	public void RisingEdge() {
		int distance = 255;
		double alpha, beta;
		
		leftMotor.forward();
		rightMotor.backward();
		// to make sure that the robot is facing the wall
		// we turn right until it sees the wall
		while (distance>dist) {
			distance=fetch();
		}
		
		// turn right until the robot detect a rising edge
		while (distance < dist) {
			distance = fetch();

		}
		// record the angle and switch directions
		Sound.beep();
		alpha = odo.getXYT()[2];
		System.out.println(alpha);
		rightMotor.forward();
		leftMotor.backward();
		// making sure the robot doesn't detect the same falling edge
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}
		// turn left until the robot detect a rising edge again
		distance = 0;
		while (distance < dist) {
			distance = fetch();

		}
		// record the second angle
		Sound.beep();
		beta = odo.getXYT()[2];

		leftMotor.stop(true);
		rightMotor.stop();
		// compute the average of the two angles detected
		System.out.println(beta);
		double avr = ((alpha + beta) / 2.0);
		System.out.println(avr);
		// calculate the change in theta by which the robot should rotate in order to
		// be facing 0deg
		double dtheta = 0;
		if (alpha > beta) {
			dtheta = 225 - avr;
		} else {
			dtheta = 180 - avr;
		}

		dtheta += odo.getXYT()[2];
		System.out.println(dtheta);
		// rotate by the calculated amount
		leftMotor.rotate(convertAngle(WHEEL_RAD, TRACK, dtheta), true);
		rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, dtheta), false);

	}

	/**
	 * This method is used to get the distance from the ultrasonic sensor
	 * 
	 * @return distance measured by the US sensor
	 */
	public int fetch() {
		int sensdistance=0;
		while (sensdistance==0) {
			usDistance.fetchSample(usData, 0); 
			
		int distance=(int) (usData[0] * 100.0);
				
		int filterControl=0;
		 if (distance >= 255 && filterControl < FILTER_OUT) {
	          // bad value, do not set the distance var, however do increment the
	          // filter value
	    	  
	          filterControl++;
	        } else if (distance >= 255) {
	          // We have repeated large values, so there must actually be nothing
	          // there: leave the distance alone
	          distance=255;
	          sensdistance=distance;
	         
	        } else {
	          // distance went below 255: reset filter and leave
	          // distance alone.
	          filterControl = 0;
	          sensdistance=distance;
	         
	        }
		}
		return sensdistance;
	}

	/**
	 * This method calculate the angle that must be passed to the motor using the
	 * radius of the wheel and the distance we want the robot to cross
	 * 
	 * @param radius
	 * @param distance
	 * @return corresponding distance
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 * This method calculate the angle that the motor must turn in order for the
	 * robot to turn by an certain angle
	 * 
	 * @param radius
	 * @param width
	 * @param angle
	 * @return Angle by which the wheels must turn
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}
