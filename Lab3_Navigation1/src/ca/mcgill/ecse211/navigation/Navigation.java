package ca.mcgill.ecse211.navigation;

import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
/**
 * This class is our main navigation class, it will follow the path chosen 
 * and avoid the obstacles it detects
 * @author Group 21
 *
 */
public class Navigation implements Runnable {
	
	public static EV3LargeRegulatedMotor rightMotor;
	public static EV3LargeRegulatedMotor leftMotor;

	public static final EV3MediumRegulatedMotor sensorMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("D"));
	// creating the sensor
	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
	private static SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
	// this instance
	private static float[] usData = new float[usDistance.sampleSize()];
	
	// instance of odometer
	private static Odometer odo;
	private static double currentX;
	private static double currentY;
	private static double currentT;
	private static double changeTheta;
	
	//holds whether or not the robot is traveling
	private static boolean travellingStatus = false;
	
	// Speeds for the motors
	private static final int FWDSPEED = 250;
	private static final int ROTATE_SPEED = 75;
	private static final int SENSOR_SPEED = 40;
	
	// initializing the 4 possible maps
	private static final int[][] Map1 = new int[][] { { 0, 2 }, { 1, 1 }, { 2, 2 }, { 2, 1 }, { 1, 0 } };
	private static final int[][] Map2 = new int[][] { { 1, 1 }, { 0, 2 }, { 2, 2 }, { 2, 1 }, { 1, 0 } };
	private static final int[][] Map3 = new int[][] { { 1, 0 }, { 2, 1 }, { 2, 2 }, { 0, 2 }, { 1, 1 } };
	private static final int[][] Map4 = new int[][] { { 0, 1 }, { 1, 2 }, { 1, 0 }, { 2, 1 }, { 2, 2 } };
	
	// initializing variable used in our methods
	private static int distance;
	private static final int safeDistance=7;
	public double Tile_Size;
	private static double theta;
	/**
	 * This is the class constructor
	 * @param Tile_Size
	 * @param leftMotor1
	 * @param rightMotor1
	 */
	public Navigation(double Tile_Size, EV3LargeRegulatedMotor leftMotor1, EV3LargeRegulatedMotor rightMotor1) {
		this.Tile_Size = Tile_Size;
		rightMotor = rightMotor1;
		leftMotor = leftMotor1;
		rightMotor.stop();
		leftMotor.stop();
		

	}
	/**
	 * The run methods makes the robot go to all the points one by one
	 * @return void
	 */
	public void run() {
		int[][] waypoints = Map1;
		for (int i = 0; i < 5; i++) {

			try {
				TravelTo(Tile_Size * waypoints[i][0], Tile_Size * waypoints[i][1]);
				
			} catch (OdometerExceptions e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	/**
	 * This method make the robot go to the position (x,y)
	 * @param x
	 * @param y
	 * @throws OdometerExceptions
	 * @throws InterruptedException
	 * @return void
	 */
	public static void TravelTo(double x, double y) throws OdometerExceptions, InterruptedException {
		theta = 0.0;
		// getting our current position
		odo = Lab3.odometer;
		currentX = odo.getXYT()[0];
		currentY = odo.getXYT()[1];
		
		while (!withinerror(currentX, currentY, x, y)) {
			
			travellingStatus = true;
			//updating our x and y
			currentX = odo.getXYT()[0];
			currentY = odo.getXYT()[1];
			// calculating the angle that we should be facing
			if (currentX == x) {
				if (currentY > y) {
					theta = Math.PI;
				} else if (currentY < y) {
					theta = 0;
				}

			} else if (currentY == y) {
				if (currentX > x) {
					theta = -Math.PI/2;
				} else if (currentX < x) {
					theta = Math.PI/2;
				}
			} else {
				theta = Math.atan((currentX - x) / (currentY - y));
				if (currentY > y) {
					theta += Math.PI;
				}
			}
			// turn to the angle calculated
			turnTo(theta * 180 / Math.PI);
			// move forward by the required distance
			leftMotor.setSpeed(FWDSPEED);
			rightMotor.setSpeed(FWDSPEED);
			// the distance is being calculated using the Pythagorean Theorem
			double dist = Math.sqrt(Math.pow((currentX - x), 2) + Math.pow((currentY - y), 2));
			leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD, dist), true);
			rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD, dist), true);
			// keep checking for obstacle and avoid them in case it found one
			while (!withinerror(currentX, currentY, x, y)) {
				// update our current X and Y
				currentX = odo.getXYT()[0];
				currentY = odo.getXYT()[1];
				// getting data from the sensor
				usSensor.fetchSample(usData, 0); 
				distance = (int) (usData[0] * 100.0); 
				// if detect Obstacle, call the Avoid and break from this loop to go back the outter loop
				if (distance < safeDistance) {
					Avoid();
					break;
				}

				try {
					Thread.sleep(50);
				} catch (Exception e) {
				}
			}
			
		}
		// arrived to its destination
		travellingStatus = false;
		

	}
	/**
	 * This method make the robot turn the angle theta
	 * @param theta
	 */
	public static void turnTo(double theta) {
		currentT = odo.getXYT()[2];
		// calculating the turn that should be done
		changeTheta = theta - currentT;
		//making sure the angle is between 0 and 360
		changeTheta = (changeTheta + 360) % 360;
		// making sure to turn by the minimal angle
		if (Math.abs(changeTheta - 360) < changeTheta) {
			changeTheta -= 360;
		}
		// make the robot turn by changeTheta
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, changeTheta), true);
		rightMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, changeTheta), false);

	}
	/**
	 * This method returns whether or not the robot is traveling
	 * @return	status 
	 */
	public static boolean isNavigating() {
		return travellingStatus;
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	/**
	 * This method return wether or not the robot reached to desired position
	 * @param Cx :currentX
	 * @param Cy : currentY
	 * @param x	:target x
	 * @param y	: traget y
	 * @return boolean 
	 */
	private static boolean withinerror(double Cx, double Cy, double x, double y) {
		double error = Math.sqrt(Math.pow((Cx - x), 2) + Math.pow((Cy - y), 2));

		return error < 2.0;
	}
	/**
	 * This method make the robot avoid the obstacle detected
	 * @return void
	 */
	public static void Avoid() {
		//make the robot turn 90 degrees to the right
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), true);
		rightMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, 90), false);
		// turn the sensor to face the obstacle
		sensorMotor.rotate(-90, false);
		// make the robot move forward until the sensor can't see the obstacle anymore
		int currentDistance = 0;
		leftMotor.setSpeed(FWDSPEED);
		rightMotor.setSpeed(FWDSPEED);
		while (currentDistance < 30) {

			leftMotor.forward();
			rightMotor.forward();
			usSensor.fetchSample(usData, 0); // acquire data
			currentDistance = (int) (usData[0] * 100.0);
		}
		// since the sensor is mounted in beginning of the robot
		// we make sure that the entire robot moved passed the obstacle
		leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD, 12), true);
		rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD, 12), false);
		// rotate the robot back to its initial direction
		// move forward until it doesn't see the other side of the obstacle
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, -90), true);
		rightMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, -90), false);
		leftMotor.setSpeed(FWDSPEED);
		rightMotor.setSpeed(FWDSPEED);
		leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD, 15), true);
		rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD, 15), false);
		do {
			leftMotor.forward();
			rightMotor.forward();
			usSensor.fetchSample(usData, 0); // acquire data
			currentDistance = (int) (usData[0] * 100.0);
		} while (currentDistance < 30);
		// making sure that the entire robot avoided the obstacle
		leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD, 10), true);
		rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD, 10), false);
		// turning back the sensor to it's initial position
		sensorMotor.rotate(90, false);
	}

}
