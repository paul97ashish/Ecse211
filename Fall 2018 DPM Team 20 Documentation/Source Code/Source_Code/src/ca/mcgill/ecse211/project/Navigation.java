package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.odometer.Odometer;
import lejos.hardware.motor.EV3LargeRegulatedMotor;


/**
 * This class is responsible for the calculations of distance and angle to reach
 * desired coordinate by taking in current position information and running set
 * calculations on the info to solve for needed variables.
 * @author Max Brodeur
 * @author Carl ElKhoury
 * @author Zakaria Essadaoui
 **/

public class Navigation {
	// Setting required fields
	public static int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 250;
	private static final double TILE_SIZE = 30.48;
	public Odometer odometer;
	private static double radius;
	private static double track;

	private static double deltaX;
	private static double deltaY;
	public static double current[];
	boolean stat = false;
	
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	boolean finished360 = false;

	public double x;
	public double y;
	
	public int speed_offset=0;
	/**
	 * Constructor class for the navigation class. 
	 * It initiate all the appropriate fields with the parameters passed to it.
	 * @param rad : radius of the robot's wheel
	 * @param Track : track of the robot
	 * @param left: left motor object
	 * @param right: right motor object
	 * @param odo : odometer object
	 */
	public Navigation(double rad, double Track,EV3LargeRegulatedMotor left , EV3LargeRegulatedMotor right, Odometer odo) {
		leftMotor=left;
		rightMotor=right;
		radius=rad;
		track=Track;
		odometer=odo;
	}

	/**
	 * This method makes the robot travel to a (x,y) 
	 * It first measures get the position of the robot from the odometer
	 * Then it calculates the angle it should be at and the distance it needs to move by
	 * 
	 * @param x
	 * @param y
	 * @return void
	 */
	public void travelTo(double x, double y) {

		this.x = x;
		this.y = y;
		
		current = odometer.getXYT(); // gets current X Y and Theta values
		deltaX = x * TILE_SIZE - current[0]; // deltaX or deltaY is the difference between where you want to go and
												// where you are currently.
		deltaY = y * TILE_SIZE - current[1];
		// System.out.println(deltaX +" " +deltaY);
		double newTheta=Math.atan2(deltaX, deltaY);
		turnTo(newTheta);
		stat = true; // boolean to say travelTo class is in action and moving forward		
		double distance=Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		move(distance);
		stat=false;
		
	}


	/**
	 * This method makes the robot move by the distance that was passed to it
	 * @param distance to move
	 * @return void
	 */
	public void move(double distance) {
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED+speed_offset);	
		int angle=convertDistance(radius, distance);
		leftMotor.rotate(angle, true);
		rightMotor.rotate(angle, false);
	}
	/**
	 * This method makes the robot turn (clockwise) by the angle that was passed to it
	 * @param theta: angle to turn
	 * @return void
	 */
	public void turnBy(double theta) {
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		int angle=convertAngle(radius, track, theta);
		leftMotor.rotate(angle, true); // turns to the origin point
		rightMotor.rotate(-angle, false);
	}
	/**
	 * This method make the robot turn to a certain angle
	 * It also make sure that it uses the minimal angle to turn
	 * @param theta: angle to which the robot should turn to
	 */
	public void turnTo(double theta) {
		
		current = odometer.getXYT();
		double deltaT = theta - Math.toRadians(current[2]);
		deltaT %= 2 * Math.PI;
		if (deltaT > Math.PI)
			deltaT -= 2 * Math.PI;
		else if (deltaT < -Math.PI)
			deltaT += 2 * Math.PI;

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		deltaT %= Math.PI * 2; // ensures robot doesn't over turn.
		int angle=convertAngle(radius, track, deltaT * 180 / Math.PI);
		leftMotor.rotate(angle, true);
		rightMotor.rotate(-angle, false);

	}

	/**
	 * Instructs the robot to spin 360 degrees in a direction specified by the
	 * boolean argument. A boolean true argument would result in clockwise 360
	 * degree turn.
	 **/

	public void turn360(boolean clockWise) {
		double angle = 360;
		if (!clockWise)
			angle = -angle;
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.rotate(convertAngle(radius, track, angle), true);
		rightMotor.rotate(-convertAngle(radius, track, angle), true);
	}
	/**
	 * This method return whether the robot is navigating or not
	 * @return boolean
	 */
	public boolean isNavigating() { // boolean created to indicate that travelTo() is currently running.
		return stat;
	}
	/**
	 * This method calculate the angle that must be passed to the motor 
	 * using the radius of the wheel and the distance we want the robot to cross
	 * @param radius
	 * @param distance
	 * @return	Corresponding angle that will make the wheels cross the desired distance
	 */
	public static int convertDistance(double radius, double distance) { // converts distance to wheel rotations
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
	public static int convertAngle(double radius, double width, double angle) { // converts angle to radians for degree
																					// rotation
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
